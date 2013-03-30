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
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.codec.DecoderOutput;
import m3da.codec.EnvelopeDecoder;
import m3da.codec.HeaderKey;
import m3da.codec.StatusCode;
import m3da.codec.dto.M3daBodyMessage;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.impl.BysantEncoderImpl;
import m3da.codec.impl.EnvelopeDecoderImpl;
import m3da.codec.impl.EnvelopeEncoderImpl;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic test for {@link M3daTcpClient}
 */
public class M3daTcpClientTest {

    private ServerSocket server;

    @Test
    public void client_send_envelope_test() throws IOException, M3daServerException, InterruptedException {
        server = new ServerSocket(0);
        int port = server.getLocalPort();
        serverThread.start();

        M3daTcpClient client = new M3daTcpClient("localhost", port, "test");

        client.connect();

        M3daBodyMessage[] msgs = client.sendEnvelope(new M3daBodyMessage[] {});
        client.close();

        serverThread.join();
    }

    private Thread serverThread = new Thread() {
        @Override
        public void run() {
            try {
                byte[] readBuffer = new byte[65536];

                Socket client;

                client = server.accept();

                int bytes = client.getInputStream().read(readBuffer);

                EnvelopeDecoder decoder = new EnvelopeDecoderImpl();
                final List<M3daEnvelope> list = new ArrayList<M3daEnvelope>();
                final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
                    @Override
                    public void decoded(final M3daEnvelope pdu) {
                        list.add(pdu);
                    }
                };

                decoder.decodeAndAccumulate(ByteBuffer.wrap(readBuffer, 0, bytes), output);

                Map<Object, Object> header = new HashMap<Object, Object>();
                header.put(HeaderKey.STATUS, StatusCode.OK.getCode());

                M3daEnvelope env = new M3daEnvelope(header, new BysantEncoderImpl().encode().array(),
                        new HashMap<Object, Object>());

                client.getOutputStream().write(new EnvelopeEncoderImpl().encode(env).array());
                client.close();
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }

        }
    };
}
