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
    public M3daBodyMessage[] sendEnvelope(M3daBodyMessage[] messages) throws IOException, DecoderException,
            M3daServerException {
        Map<Object, Object> header = new HashMap<Object, Object>();
        header.put(HeaderKey.ID, clientId);
        M3daEnvelope envelope = new M3daEnvelope(header, bysantEncoder.encode(messages).array(),
                new HashMap<Object, Object>());

        ByteBuffer buffer = encoder.encode(envelope);
        socket.getOutputStream().write(buffer.array());

        final List<M3daEnvelope> list = new ArrayList<M3daEnvelope>();
        final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
            @Override
            public void decoded(final M3daEnvelope pdu) {
                list.add(pdu);
            }
        };

        do {
            // read some bytes
            final int bytes = socket.getInputStream().read(readBuffer);
            if (bytes <= 0) {
                throw new RuntimeException("connection closed");
            }
            decoder.decodeAndAccumulate(ByteBuffer.wrap(readBuffer, 0, bytes), output);
        } while (list.size() == 0);
        decoder.finishDecode();

        M3daEnvelope env = list.get(0);
        byte[] payload = env.getPayload();

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        socket.close();
    }

}