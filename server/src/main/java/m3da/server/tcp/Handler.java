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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.codec.BysantDecoder;
import m3da.codec.BysantEncoder;
import m3da.codec.DecoderOutput;
import m3da.codec.HeaderKey;
import m3da.codec.M3daCodecService;
import m3da.codec.dto.M3daDeltasVector;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.dto.M3daMessage;
import m3da.codec.dto.M3daPdu;
import m3da.codec.dto.M3daQuasiPeriodicVector;
import m3da.server.store.Envelope;
import m3da.server.store.Message;
import m3da.server.store.StoreService;

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
    private StoreService store;

    private M3daCodecService codec;

    public Handler(StoreService store, M3daCodecService codec) {
        this.store = store;
        this.codec = codec;
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

            final String comId = new String(((ByteBuffer) env.getHeader().get(HeaderKey.ID)).array(),
                    Charset.forName("UTF8"));
            LOG.info("client communication identifier : {}", comId);

            if (env.getPayload().length > 0) {
                BysantDecoder decoder = (BysantDecoder) session.getAttribute("decoder");
                ListDecoder out = new ListDecoder();
                decoder.decodeAndAccumulate(ByteBuffer.wrap(env.getPayload()), out);
                List<Object> decoded = out.list;
                List<Message> data = new ArrayList<Message>(decoded.size());

                for (Object o : decoded) {
                    if (o instanceof M3daMessage) {
                        M3daMessage msg = (M3daMessage) o;

                        // uncompress list of values (quasicperiodic vector, etc..)
                        Map<String, List<?>> bodyData = new HashMap<String, List<?>>();
                        for (Map.Entry<Object, Object> e : msg.getBody().entrySet()) {
                            bodyData.put(e.getKey().toString(), extractList(e.getValue()));
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
                    pdus[i] = new M3daMessage(toSend.get(i).getPath(), 0L, new HashMap<Object, Object>(toSend.get(i)
                            .getData()));
                }

            }

            BysantEncoder encoder = (BysantEncoder) session.getAttribute("encoder");

            // encode the message to be sent
            byte[] binaryPayload = encoder.encode(pdus).array();
            // enqueue for socket writing
            Map<Object, Object> header = new HashMap<Object, Object>();
            header.put(HeaderKey.STATUS, 200);
            session.write(new M3daEnvelope(header, binaryPayload, new HashMap<Object, Object>()));

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
}
