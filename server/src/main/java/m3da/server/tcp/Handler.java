/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package m3da.server.tcp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.codec.BysantDecoder;
import m3da.codec.BysantEncoder;
import m3da.codec.DecoderException;
import m3da.codec.DecoderOutput;
import m3da.codec.HeaderKey;
import m3da.codec.M3daCodecService;
import m3da.codec.dto.M3daDeltasVector;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.dto.M3daMessage;
import m3da.codec.dto.M3daPdu;
import m3da.codec.dto.M3daQuasiPeriodicVector;
import m3da.server.session.M3daAuthentication;
import m3da.server.session.M3daSecurityInfo;
import m3da.server.session.M3daSession;
import m3da.server.store.DataValue;
import m3da.server.store.Envelope;
import m3da.server.store.Message;
import m3da.server.store.SecurityStore;
import m3da.server.store.Store;
import m3da.server.tcp.security.AuthenticationResult;
import m3da.server.tcp.security.PasswordNegoHandler;
import m3da.server.tcp.security.PasswordNegoState;
import m3da.server.tcp.security.SecurityHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * I/O logic handler for the M3DA protocol : store received data and push pending data for this client.
 */
public class Handler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Handler.class);
    private final Store store;

    private final M3daCodecService codec;

    private final PasswordNegoHandler passNego;

    private final SecurityHandler securityHandler;

    public Handler(Store store, SecurityStore securityStore, M3daCodecService codec) {
        this.store = store;
        this.codec = codec;
        this.passNego = new PasswordNegoHandler(securityStore);
        this.securityHandler = new SecurityHandler(securityStore);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOG.error("unexpected exception : ", cause);
        session.close(true);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {

        session.setAttribute("decoder", codec.createBodyDecoder());
        session.setAttribute("encoder", codec.createBodyEncoder());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        session.close(false);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message instanceof M3daEnvelope) {
            M3daEnvelope env = (M3daEnvelope) message;

            M3daSession m3daSession = getSession(session);

            // password negotiation needed ?
            M3daSecurityInfo secInfo = m3daSession.getCommunicationInfo();
            if (secInfo != null && !M3daAuthentication.NONE.equals(secInfo.getM3daSecurityType())
                    && StringUtils.isBlank(secInfo.getM3daCredential())) {
                negotiatePassword(env, m3daSession, session);
            } else {
                // the negotiation is done, so we respond
                respond(session, env);
            }

        } else {
            LOG.error("should be M3daEnvelope, not {}", message.getClass().getCanonicalName());
            // die die buggy client
            session.close(true);
        }
    }

    private void respond(final IoSession session, M3daEnvelope env) throws Exception {
        final String comId = new String(((ByteBuffer) env.getHeader().get(HeaderKey.ID)).array(),
                Charset.forName("UTF8"));
        LOG.info("client communication identifier : {}", comId);

        AuthenticationResult result = securityHandler.authenticate(env, getSession(session));
        if (result.isSuccess()) {
            // handle this message
            final M3daEnvelope response = createResponse(comId, result.getEnvelope(), session);
            if (response == null) {
                LOG.error("due to session {} buggy header we close the connection", session);
                // nothing to respond : buggy header
                session.close(true);
            } else {
                // apply security if needed
                session.write(securityHandler.signResponse(response, getSession(session)));
            }
        } else {
            // send the error message to the device
            session.write(securityHandler.signResponse(result.getEnvelope(), getSession(session)));
            if (result.getEndSession()) {
                session.close(false);
            }
        }

    }

    private M3daEnvelope createResponse(String comId, M3daEnvelope env, IoSession session) throws DecoderException {
        if (env.getPayload().length > 0) {
            long now = System.currentTimeMillis();

            BysantDecoder decoder = (BysantDecoder) session.getAttribute("decoder");
            ListDecoder out = new ListDecoder();
            decoder.decodeAndAccumulate(ByteBuffer.wrap(env.getPayload()), out);
            List<Object> decoded = out.list;
            List<Message> data = new ArrayList<Message>(decoded.size());

            for (Object o : decoded) {
                if (o instanceof M3daMessage) {
                    M3daMessage msg = (M3daMessage) o;

                    Map<String, List<DataValue<?>>> bodyData = new HashMap<String, List<DataValue<?>>>();
                    // is there timestamp ??

                    if (isCorrelatedData(msg.getBody())) {
                        LOG.debug("correlated data (joy joy..)");
                        // do we have timestamps ?
                        if (msg.getBody().containsKey("timestamp") || msg.getBody().containsKey("timestamps")) {
                            createTimestampedCorrelatedData(bodyData, msg);
                        } else {
                            createVersionedCorrelatedData(bodyData, msg);
                        }
                    } else {

                        // uncompress list of values (quasicperiodic vector, etc..)
                        for (Map.Entry<Object, Object> e : msg.getBody().entrySet()) {
                            List<?> values = extractList(e.getValue());
                            List<DataValue<?>> dataValues = new ArrayList<DataValue<?>>(values.size());

                            for (Object v : values) {
                                dataValues.add(new DataValue<Object>(now, v));
                            }

                            bodyData.put(e.getKey().toString(), dataValues);
                        }
                    }

                    data.add(new Message(msg.getPath(), bodyData));
                }
            }
            store.enqueueReceivedData(comId, System.nanoTime(), new Envelope(System.currentTimeMillis(), data));
        }

        M3daPdu[] pdus = new M3daPdu[0];
        // do we have pending data for this client ?
        List<Message> toSend = store.popDataToSend(comId);
        if (toSend != null && toSend.size() > 0) {
            // convert to the encoder DTO
            pdus = new M3daPdu[toSend.size()];
            for (int i = 0; i < pdus.length; i++) {
                Map<String, List<DataValue<?>>> data = toSend.get(i).getData();

                Map<Object, Object> toSerialize = new HashMap<Object, Object>();

                for (Map.Entry<String, List<DataValue<?>>> e : data.entrySet()) {
                    if (e.getValue() == null || e.getValue().size() == 0) {
                        toSerialize.put(e.getKey(), null);
                    } else if (e.getValue().size() == 1) {
                        toSerialize.put(e.getKey(), e.getValue().get(0).getValue());
                    } else {
                        List<Object> value = new ArrayList<Object>(e.getValue().size());
                        for (DataValue<?> d : e.getValue()) {
                            value.add(d.getValue());
                        }
                        toSerialize.put(e.getKey(), e.getValue());
                    }
                }

                pdus[i] = new M3daMessage(toSend.get(i).getPath(), 0L, toSerialize);
            }

        }

        BysantEncoder encoder = (BysantEncoder) session.getAttribute("encoder");

        // encode the message to be sent
        byte[] binaryPayload = encoder.encode(pdus).array();
        // enqueue for socket writing
        Map<Object, Object> header = new HashMap<Object, Object>();
        header.put(HeaderKey.STATUS, 200);
        return new M3daEnvelope(header, binaryPayload, new HashMap<Object, Object>());
    }

    private void negotiatePassword(M3daEnvelope env, M3daSession m3daSession, IoSession session) throws IOException {
        M3daEnvelope envNego = passNego.handle(env, m3daSession);

        if (envNego != null) {
            // we are in middle of a password negotiation, send back the reply
            session.write(envNego);
        } else if (m3daSession.getPassNegoState() == PasswordNegoState.DONE) {
            // password negotiation ended successfully
            // let's wait for more message, the system will send more (or not)
        } else {
            throw new IllegalStateException("negotiation response expected");
        }
    }

    /**
     * Return <code>true</code> if the body contains correlated data (list of multiple values)
     */
    private boolean isCorrelatedData(final Map<Object, Object> body) {
        for (final Map.Entry<Object, Object> e : body.entrySet()) {
            if (e.getValue() instanceof List || e.getValue() instanceof M3daDeltasVector
                    || e.getValue() instanceof M3daQuasiPeriodicVector) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create the {@link Item} for a message containing correlated data with timestamp
     */
    private void createTimestampedCorrelatedData(final Map<String, List<DataValue<?>>> bodyData, final M3daMessage msg) {
        LOG.debug("timestamped correlated data");
        final Map<Object, Object> body = msg.getBody();

        // correlated data with timestamp
        List<?> ts = null;
        if (body.containsKey("timestamp") || body.containsKey("timestamps")) {
            if (body.containsKey("timestamp")) {
                ts = asFlatList(body.get("timestamp"));
            } else {
                ts = asFlatList(body.get("timestamps"));
            }
        }
        for (final Map.Entry<Object, Object> e : msg.getBody().entrySet()) {
            // skip timestamp, it's the "index"
            if ("timestamp".equals(e.getKey()) || "timestamps".equals(e.getKey())) {
                continue;
            }
            int index = 0;
            long lastDate = System.currentTimeMillis();
            List<DataValue<?>> valuesForKey = new ArrayList<DataValue<?>>();
            for (Object value : extractList(e.getValue())) {
                if (value instanceof ByteBuffer) {
                    try {
                        value = new String(((ByteBuffer) value).array(), "UTF-8");
                    } catch (UnsupportedEncodingException e1) {
                        throw new IllegalStateException("no UTF-8 codec in the JVM");
                    }
                }
                if (index < ts.size()) {
                    final long date = Long.valueOf(ts.get(index).toString()) * 1000L;
                    valuesForKey.add(new DataValue<Object>(date, value));
                    lastDate = date;
                } else {
                    // use last date plus one milliseconds to be sure it's unique
                    lastDate++;
                    valuesForKey.add(new DataValue<Object>(lastDate, value));
                }
                index++;
            }
            bodyData.put(e.getKey().toString(), valuesForKey);
        }
    }

    /**
     * Create the {@link Item} for a message containing correlated data without timestamp
     */
    private void createVersionedCorrelatedData(final Map<String, List<DataValue<?>>> bodyData, final M3daMessage msg) {
        LOG.debug("correlated data with no timestamp");
        // correlated data with no timestamp so we keep the last value
        long now = System.currentTimeMillis();
        for (final Map.Entry<Object, Object> e : msg.getBody().entrySet()) {

            List<DataValue<?>> values = new ArrayList<DataValue<?>>();
            for (Object o : extractList(e.getValue())) {
                values.add(new DataValue<Object>(now, o));
            }
            bodyData.put(e.getKey().toString(), values);
        }
    }

    /**
     * Extract a list of value following the M3DA convention : extract QuasiPeriodic and Delta vectors. Convert non list
     * item to list with one element
     */
    private List<?> extractList(final Object v) {
        List<?> valueList;
        if (v instanceof List) {
            valueList = (List<?>) v;
        } else if (v instanceof M3daDeltasVector) {
            valueList = ((M3daDeltasVector) v).asFlatList();
        } else if (v instanceof M3daQuasiPeriodicVector) {
            valueList = ((M3daQuasiPeriodicVector) v).asFlatList();
        } else if (v instanceof ByteBuffer) {
            // as String (TODO : handle binary data)
            valueList = Collections.singletonList(new String(((ByteBuffer) v).array(), Charset.forName("UTF8")));
        } else {
            valueList = Collections.singletonList(v);
        }
        return valueList;
    }

    private List<?> asFlatList(Object encodedTs) {
        if (encodedTs instanceof List) {
            return (List<?>) encodedTs;
        } else if (encodedTs instanceof M3daQuasiPeriodicVector) {
            return ((M3daQuasiPeriodicVector) encodedTs).asFlatList();
        } else if (encodedTs instanceof M3daDeltasVector) {
            return ((M3daDeltasVector) encodedTs).asFlatList();
        } else {
            return null;
        }
    }

    /**
     * Decoder output accumulating the data in a list
     */
    private static class ListDecoder implements DecoderOutput<M3daPdu> {
        private final List<Object> list = Lists.newArrayList();

        /**
         * {@inheritDoc}
         */
        @Override
        public void decoded(final M3daPdu pdu) {
            list.add(pdu);
        }
    }

    private M3daSession getSession(IoSession ioSession) {
        return (M3daSession) ioSession.getAttribute(ComInfoFilter.M3DA_SESSION);
    }
}
