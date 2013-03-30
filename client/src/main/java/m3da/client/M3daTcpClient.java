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
package m3da.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.codec.BysantDecoder;
import m3da.codec.BysantEncoder;
import m3da.codec.DecoderException;
import m3da.codec.DecoderOutput;
import m3da.codec.EnvelopeDecoder;
import m3da.codec.EnvelopeEncoder;
import m3da.codec.HeaderKey;
import m3da.codec.M3daCodecService;
import m3da.codec.StatusCode;
import m3da.codec.dto.M3daBodyMessage;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.dto.M3daPdu;
import m3da.codec.impl.M3daCodecServiceImpl;

/**
 * M3DA TCP client. Use it for connecting to a M3DA server.
 * <p>
 * Usage :
 * 
 * <pre>
 * M3daTcpClient client = new M3daTcpClient("m2m.eclipse.org", 44900);
 * client.connect();
 * M3daEnvelope env = new M3daEnvelope();
 * ..
 * 
 * M3daEnvelope receivedEnv = client.send(env);
 * client.close();
 * </pre>
 */
public class M3daTcpClient implements M3daClient {

    private final String host;

    private final int port;

    private final String clientId;

    private final BysantEncoder bysantEncoder;

    private final BysantDecoder bysantDecoder;

    private final EnvelopeEncoder encoder;

    private final EnvelopeDecoder decoder;

    private final byte[] readBuffer = new byte[65536];

    private Socket socket;

    /**
     * Create a TCP M3DA client.
     * 
     * @param host the server hostname or IP address
     * @param port the TCP port to use (should be 44900)
     * @param clientId te client unique identifier
     */
    public M3daTcpClient(String host, int port, String clientId) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;

        M3daCodecService codec = new M3daCodecServiceImpl();
        encoder = codec.createEnvelopeEncoder();
        decoder = codec.createEnvelopeDecoder();

        bysantEncoder = codec.createBodyEncoder();
        bysantDecoder = codec.createBodyDecoder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(2000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public M3daBodyMessage[] sendEnvelope(M3daBodyMessage[] messages) throws IOException, M3daServerException {
        if (socket == null) {
            throw new IllegalStateException("you need to connect first");
        }

        // encode the envelope

        Map<Object, Object> header = new HashMap<Object, Object>();
        header.put(HeaderKey.ID, clientId);
        M3daEnvelope envelope = new M3daEnvelope(header, bysantEncoder.encode(messages).array(),
                new HashMap<Object, Object>());

        // send to the server
        ByteBuffer buffer = encoder.encode(envelope);
        socket.getOutputStream().write(buffer.array());

        // read and decode received bytes
        final List<M3daEnvelope> list = new ArrayList<M3daEnvelope>();
        final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
            @Override
            public void decoded(final M3daEnvelope pdu) {
                list.add(pdu);
            }
        };

        try {
            do {
                // read some bytes
                final int bytes = socket.getInputStream().read(readBuffer);
                if (bytes <= 0) {
                    return null;
                }
                decoder.decodeAndAccumulate(ByteBuffer.wrap(readBuffer, 0, bytes), output);
            } while (list.size() == 0);

            decoder.finishDecode();

            M3daEnvelope env = list.get(0);
            byte[] payload = env.getPayload();

            // throw the correct exception in case of server side error
            if (((Integer) env.getHeader().get(HeaderKey.STATUS)) != StatusCode.OK.getCode()) {
                throw new M3daServerException(StatusCode.fromCode((Integer) env.getHeader().get(HeaderKey.STATUS)));
            }

            final List<M3daBodyMessage> body = new ArrayList<M3daBodyMessage>();
            final DecoderOutput<M3daPdu> bodyOutput = new DecoderOutput<M3daPdu>() {
                @Override
                public void decoded(final M3daPdu pdu) {
                    body.add((M3daBodyMessage) pdu);
                }
            };

            bysantDecoder.decodeAndAccumulate(ByteBuffer.wrap(payload), bodyOutput);
            return body.toArray(new M3daBodyMessage[] {});
        } catch (DecoderException e) {
            throw new IllegalStateException("cannot decode the server message", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        socket.close();
    }

}