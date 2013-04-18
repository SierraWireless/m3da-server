package m3da.server.tcp.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import m3da.codec.BysantEncoder;
import m3da.codec.DecoderException;
import m3da.codec.DecoderOutput;
import m3da.codec.EnvelopeDecoder;
import m3da.codec.HeaderKey;
import m3da.codec.Hex;
import m3da.codec.M3daCodecService;
import m3da.codec.M3daCodecService.CipherMode;
import m3da.codec.StatusCode;
import m3da.codec.dto.CipherAlgorithm;
import m3da.codec.dto.HmacType;
import m3da.codec.dto.M3daEnvelope;
import m3da.server.session.M3daCipher;
import m3da.server.session.M3daSecurityInfo;
import m3da.server.session.M3daSecurityType;
import m3da.server.session.M3daSession;
import m3da.server.store.SecurityStore;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class in charge of authenticating the incoming envelopes and signing the responses.
 */
public class SecurityHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityHandler.class);

    private M3daCodecService codec;

    /** for reading and storing client information */
    private final SecurityStore securityStore;

    /** maximum retry count, after that we kick the client */
    private static final int MAX_AUTH_ATTEMPT = 1;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public SecurityHandler(SecurityStore securityStore) {
        this.securityStore = securityStore;
    }

    /**
     * Try to authenticate the first envelope.
     * <p>
     * If the system does not require security, the incoming envelope is considered as authenticated.
     * <p>
     * If authentication is needed, the HMAC signature is checked. The nonce desynchronization is handled (device or
     * server challenge). When an envelope is authenticated, its content is deciphered if needed and decoded.
     * 
     * @param env the envelope to authenticate
     * @param session the current session
     * @return The result of the authentication processing. Can be an error to send back to the device or a protected
     *         envelope to be processed.
     * @throws DecoderException
     */
    public AuthenticationResult authenticate(M3daEnvelope env, M3daSession session) throws SecurityException,
            DecoderException {
        LOG.debug("authenticating envelope : {}", env);

        if (env.getHeader().get(HeaderKey.ID) == null || !(env.getHeader().get(HeaderKey.ID) instanceof ByteBuffer)) {
            // doh ! no communication identifier, we can't identify this system
            throw new IllegalArgumentException("received communication without any system communication identifier");
        }

        final String communicationId = bbToStr((ByteBuffer) env.getHeader().get(HeaderKey.ID));

        // is the security info already loaded in the session ?
        M3daSecurityInfo secInfo = session.getCommunicationInfo();
        if (secInfo == null) {
            secInfo = securityStore.getSecurityInfo(communicationId);
            if (secInfo == null) {
                throw new SecurityException("no security information for device with commid : " + communicationId);
            }

            session.setCommunicationInfo(secInfo);
            session.setCommunicationId(communicationId);
        }

        LOG.debug("security information : {}", secInfo);

        switch (secInfo.getM3daSecurityType()) {
        case NONE:
            // no auth, let's pass thru
            return AuthenticationResult.success(env);
        case HMAC_MD5:
        case HMAC_SHA1:
            return authenticate(env, session, secInfo);
        default:
            throw new IllegalStateException("unsupported security type : " + secInfo.getM3daSecurityType());
        }
    }

    /**
     * Check the message signature and decipher the content if needed
     * 
     * @throws DecoderException
     */
    private AuthenticationResult authenticate(M3daEnvelope env, M3daSession session, M3daSecurityInfo secInfo)
            throws SecurityException, DecoderException {

        if (!env.getFooter().containsKey(HeaderKey.MAC)) {
            LOG.debug("no MAC in the footer");

            final String chal = bbToStr((ByteBuffer) env.getHeader().get(HeaderKey.CHALLENGE));
            if (chal != null) {
                // it's a challenge, so we should have a nonce
                final byte[] newNonce = ((ByteBuffer) env.getHeader().get(HeaderKey.NONCE)).array();
                final String newNonceHex = Hex.encodeHexString(newNonce);
                LOG.debug("received challenge nonce : {}", newNonceHex);

                secInfo.setM3daNonce(newNonceHex);

                M3daEnvelope lastResponse = session.getLastServerResponse();
                if (lastResponse == null) {
                    // the server should not be challenged if a response was not sent
                    throw new SecurityException("unexpected challenge from device with commId "
                            + session.getCommunicationId());
                } else {
                    return AuthenticationResult.failure(lastResponse, false);
                }
            } else {
                // we send a challenge, we want authentication (407)
                return sendChallenge(StatusCode.AUTHENTICATION_REQUIRED, secInfo.getM3daSecurityType(), session);
            }
        } else {
            final String communicationId = session.getCommunicationId();

            final byte[] receivedMac = ((ByteBuffer) env.getFooter().get(HeaderKey.MAC)).array();

            // same credential for both client and server
            final byte[] password = secInfo.getM3daCredential().getBytes(UTF_8);
            String hexNonce = secInfo.getM3daNonce();
            if (hexNonce == null) {
                // random nonce to force a challenge
                hexNonce = "";
            }

            byte[] serverNonce = Hex.decodeHex(hexNonce);
            final byte[] clientUsername = communicationId.getBytes(UTF_8);

            byte[] computedMac = codec.hmac(HmacType.getHmacType(secInfo.getM3daSecurityType().getDescription()),
                    clientUsername, password, serverNonce, env.getPayload());

            if (LOG.isDebugEnabled()) {
                LOG.debug("computed HMAC : {} - received HMAC : {}", Hex.encodeHexString(computedMac),
                        Hex.encodeHexString(receivedMac));
            }
            if (Arrays.equals(receivedMac, computedMac)) {
                LOG.debug("success ! the client {} is authenticated", communicationId);

                // reset the number of authentication attempt
                session.setClientAuthenticationAttemptCount(0);

                // decipher the payload if needed
                byte[] content = env.getPayload();
                if (!M3daCipher.NONE.equals(secInfo.getM3daCipher())) {
                    // deciphering
                    CipherAlgorithm cipherAlgo = CipherAlgorithm.getCipher(secInfo.getM3daCipher().getDescription());
                    LOG.debug("deciphering content");

                    ByteArrayOutputStream deciphered = new ByteArrayOutputStream();
                    codec.cipher(CipherMode.DECRYPTION, cipherAlgo, password, serverNonce,
                            new ByteArrayInputStream(env.getPayload()), deciphered);

                    content = deciphered.toByteArray();

                    try {
                        deciphered.close();
                    } catch (Exception e) {
                        // quiet..
                    }
                }

                // extract the contained protected envelope and send it to the next filter
                final EnvelopeDecoder envDec = codec.createEnvelopeDecoder();
                final List<M3daEnvelope> decodedEnvs = new ArrayList<M3daEnvelope>();
                envDec.decodeAndAccumulate(ByteBuffer.wrap(content), new DecoderOutput<M3daEnvelope>() {
                    @Override
                    public void decoded(final M3daEnvelope pdu) {
                        decodedEnvs.add(pdu);
                    }
                });
                envDec.finishDecode();

                if (decodedEnvs.size() != 1) {
                    LOG.warn("empty envelope sent by system {}", communicationId);
                    // no envelope ?
                    // TODO: send some error
                }
                final M3daEnvelope protectedEnv = decodedEnvs.get(0);
                // add the communication ID
                final Map<Object, Object> header = new HashMap<Object, Object>(protectedEnv.getHeader());
                header.put(HeaderKey.ID, ByteBuffer.wrap(communicationId.getBytes(UTF_8)));

                // store the new nonce if any
                final ByteBuffer nonce = (ByteBuffer) protectedEnv.getHeader().get(HeaderKey.NONCE);
                if (nonce != null) {
                    secInfo.setM3daNonce(Hex.encodeHexString(nonce.array()));
                }

                // process the request (only the inner envelope)
                return AuthenticationResult.success(new M3daEnvelope(header, protectedEnv.getPayload(), protectedEnv
                        .getFooter()));
            } else {
                LOG.debug("HMAC not matching");
                // crap it's not matching, send back 401 with a new challenge
                return sendChallenge(StatusCode.UNAUTHORIZED, secInfo.getM3daSecurityType(), session);
            }
        }
    }

    /**
     * Send a new challenge or an error if the maximum number of retry is exceeded
     */
    private AuthenticationResult sendChallenge(final StatusCode statusCode, M3daSecurityType securityType,
            M3daSession session) throws SecurityException {
        int attempt = session.getClientAuthenticationAttemptCount();
        if (attempt < MAX_AUTH_ATTEMPT) {
            M3daEnvelope envelope = this.createEmptyEnvelope();
            envelope.getHeader().put(HeaderKey.STATUS, statusCode.getCode());
            envelope.getHeader().put(HeaderKey.CHALLENGE, securityType.getDescription());
            session.setClientAuthenticationAttemptCount(attempt + 1);
            return AuthenticationResult.failure(envelope, false);
        } else {
            throw new SecurityException("too many challenges requested by the server for device with commId : "
                    + session.getCommunicationId());
        }
    }

    /**
     * Sign the response envelope if authentication is required. The protected envelope is ciphered if needed.
     * 
     * @param response the private envelope
     * @param session the current session
     * @return the signed response
     */
    public M3daEnvelope signResponse(final M3daEnvelope response, final M3daSession session) {
        LOG.debug("signing response : {}", response);

        // add new NONCE and signature if needed
        final M3daSecurityInfo secInfo = session.getCommunicationInfo();
        if (secInfo == null || secInfo.getM3daSecurityType() == M3daSecurityType.NONE) {
            LOG.debug("no security for this system, we push the message");
            // no security, we push the message
            return response;

        } else {
            final byte[] nextNonce = generateNonce();
            final String nextHexNonce = Hex.encodeHexString(nextNonce);
            LOG.debug("generated new nonce : {}", nextHexNonce);
            response.getHeader().put(HeaderKey.NONCE, nextNonce);

            // is it a challenge ? don't sign the message
            if (response.getHeader().containsKey(HeaderKey.CHALLENGE)) {
                LOG.debug("server challenge, not signed");
                // save the nonce
                secInfo.setM3daNonce(nextHexNonce);
                // push the challenge envelope
                return response;

            } else {
                // store the response to send it again if the server is challenged
                session.setLastServerResponse(response);

                final Map<Object, Object> header = new HashMap<Object, Object>();
                header.put(HeaderKey.ID, ByteBuffer.wrap(session.getCommunicationId().getBytes(UTF_8)));

                // copy error code if security error. not sure about this...
                Object status = response.getHeader().get(HeaderKey.STATUS);
                if (status != null && ((Integer) status).equals(StatusCode.ENCRYPTION_NEEDED.getCode())) {
                    header.put(HeaderKey.STATUS, status);
                }

                byte[] payload = codec.createEnvelopeEncoder().encode(response).array();

                // same password for both client and server
                final byte[] password = secInfo.getM3daCredential().getBytes(UTF_8);
                byte[] serverNonce = Hex.decodeHex(secInfo.getM3daNonce());

                if (!secInfo.getM3daCipher().equals(M3daCipher.NONE)) {
                    // ciphering
                    CipherAlgorithm cipherAlgo = CipherAlgorithm.getCipher(secInfo.getM3daCipher().getDescription());

                    LOG.debug("ciphering payload with algo : {}", cipherAlgo);

                    ByteArrayOutputStream cipherOut = new ByteArrayOutputStream();
                    codec.cipher(CipherMode.ENCRYPTION, cipherAlgo, password, serverNonce, new ByteArrayInputStream(
                            payload), cipherOut);

                    payload = cipherOut.toByteArray();
                    try {
                        cipherOut.close();
                    } catch (Exception e) {
                        // quiet
                    }
                }

                // compute HMAC
                byte[] computedMac = codec.hmac(HmacType.getHmacType(secInfo.getM3daSecurityType().getDescription()),
                        M3daCodecService.SERVER_NAME.getBytes(UTF_8), password, serverNonce, payload);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("computed HMAC : {}", Hex.encodeHexString(computedMac));
                }

                final Map<Object, Object> footer = new HashMap<Object, Object>();
                footer.put(HeaderKey.MAC, new ByteBuffer[] { ByteBuffer.wrap(computedMac) });

                // store the generated nonce
                secInfo.setM3daNonce(nextHexNonce);

                return new M3daEnvelope(header, payload, footer);
            }
        }
    }

    /**
     * Store the nonce for future communications when the current session ends.
     * 
     * @param session the ending session
     * @throws CommunicationServiceException
     */
    public void sessionClosed(M3daSession session) {
        final M3daSecurityInfo secInfo = session.getCommunicationInfo();
        if (secInfo != null && !M3daSecurityType.NONE.equals(secInfo.getM3daSecurityType())) {

            final String communicationId = session.getCommunicationId();
            if (communicationId == null) {
                LOG.error("no communication saved on this session, but we have a security info : BUG ?");

            } else if (StringUtils.isNotBlank(secInfo.getM3daNonce())) {
                securityStore.storeNonce(communicationId, secInfo.getM3daNonce());
            }
        }
    }

    private M3daEnvelope createEmptyEnvelope() {
        final Map<Object, Object> header = new HashMap<Object, Object>();

        final BysantEncoder enc = codec.createBodyEncoder();
        final ByteBuffer buffer = enc.encode();

        return new M3daEnvelope(header, buffer.array(), Collections.emptyMap());
    }

    private Random rng = new Random();

    /** generate 128bit random nonce */
    private byte[] generateNonce() {
        try {
            return md5((Double.toString(rng.nextDouble()) + Double.toString(rng.nextDouble())).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /** convert an UTF8 encoded ByteBuffer to java string */
    private String bbToStr(final ByteBuffer src) {
        if (src == null) {
            return null;
        }
        return new String(src.array(), UTF_8);
    }

    private byte[] md5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            return digest.digest(data);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("no MD5 provider in the JVM");
        }
    }
}
