package m3da.server.tcp.security;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import m3da.codec.DecoderException;
import m3da.codec.DecoderOutput;
import m3da.codec.EcdhService;
import m3da.codec.HeaderKey;
import m3da.codec.Hex;
import m3da.codec.M3daCodecService;
import m3da.codec.StatusCode;
import m3da.codec.dto.HmacType;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.impl.EcdhServiceImpl;
import m3da.codec.impl.M3daCodecServiceImpl;
import m3da.server.session.M3daSecurityInfo;
import m3da.server.session.M3daSession;
import m3da.server.store.SecurityStore;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * Implements the logic for password auto-negotiation at runtime on both the device and server system. This alleviate
 * the need for randomly generated passwords at manufacturing time.
 * 
 * The negotiation is done in 3 steps :
 * <ul>
 * <li>salt exchange for avoiding replay of previous negotiation exchanges</li>
 * <li>Diffie-Hellman pub key exchange and new password generation</li>
 * <li>acknowledge from the client of the correct reception of the new password</li>
 * </ul>
 */
public class PasswordNegoHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordNegoHandler.class);

    /** encoding decoding M3DA messages */
    private M3daCodecService codec = new M3daCodecServiceImpl();

    /** for Elliptic Curve based Diffie Hellman */
    private EcdhService eccdh = new EcdhServiceImpl();

    /** for generating random salt */
    private Random rng = new Random();

    /** for storing generated password */
    private final SecurityStore securityStore;

    public PasswordNegoHandler(SecurityStore securityStore) {
        this.securityStore = securityStore;
    }

    /**
     * Handle the negotiation
     */
    public M3daEnvelope handle(M3daEnvelope env, M3daSession session) throws PasswordNegotiationException {

        if (StringUtils.isNotBlank(session.getCommunicationInfo().getM3daCredential())) {
            throw new IllegalStateException("credential already provisioned");
        }

        switch (session.getPassNegoState()) {
        case NONE:
            return startNegotiation(env, session);
        case WAIT_PUB_KEY:
            return sendPwd(env, session);
        case WAIT_ACK:
            storeNewPassword(env, session);
            return null;
        default:
            throw new IllegalStateException("invalid password negotiation state : " + session.getPassNegoState());
        }

    }

    /** start the password negotiation : receive client salt and send back server salt */
    private M3daEnvelope startNegotiation(M3daEnvelope env, M3daSession session) {
        ByteBuffer salt = (ByteBuffer) env.getHeader().get(HeaderKey.AUTOREG_SALT);
        if (salt != null) {
            LOG.debug("negotiation step 1 : salt exchange");

            // the system want to start the negotiation
            M3daSecurityInfo comInfo = session.getCommunicationInfo();

            if (StringUtils.isBlank(comInfo.getM3daSharedKey())) {
                throw new PasswordNegotiationException("negotiation not allowed : no registration password");
            }

            session.setPassNegoClientSalt(salt.array());

            byte[] serverSalt = new byte[16];
            rng.nextBytes(serverSalt);
            session.setPassNegoServerSalt(serverSalt);

            Map<Object, Object> header = new HashMap<Object, Object>();
            header.put(HeaderKey.AUTOREG_SALT, serverSalt);
            session.setPassNegoState(PasswordNegoState.WAIT_PUB_KEY);
            return new M3daEnvelope(header, new byte[] {}, new HashMap<Object, Object>());
        } else {
            // device should start the negotiation or credentials should be provided manually
            throw new PasswordNegotiationException("password negotiation expected : no auto_reg salt received");
        }
    }

    /** receive public client key, generate shared secret and send the new password (shared secret ciphered) */
    private M3daEnvelope sendPwd(M3daEnvelope env, M3daSession session) {
        LOG.debug("negotiation step 2 : public key exchange");

        // check secured envelop signature
        M3daEnvelope securedEnv = checkSecurityAndDecode(env, session);

        // grab the other side public key
        ByteBuffer pubKey = (ByteBuffer) securedEnv.getHeader().get(HeaderKey.AUTOREG_PUBKEY);

        if (pubKey == null) {
            throw new PasswordNegotiationException("no pub key in the message");
        }

        KeyPair keyPair = eccdh.generateEcdhKeyPair();
        byte[] sharedSecret = eccdh.computeSharedSecret(keyPair, pubKey.array());

        // generate new password
        String newPassword = RandomStringUtils.random(50, true, true);
        byte[] md5Password;
        try {
            md5Password = md5(newPassword.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("new password = {}, md5 = {}", newPassword, Hex.encodeHexString(md5Password));
        }

        Map<Object, Object> header = new HashMap<Object, Object>();
        header.put(HeaderKey.AUTOREG_PUBKEY, eccdh.getPublicKeyCertificate(keyPair));
        header.put(HeaderKey.AUTOREG_CTEXT, eccdh.cipherWithSecret(sharedSecret, md5Password));

        // encode the envelope to secure
        byte[] payload = codec.createEnvelopeEncoder()
                .encode(new M3daEnvelope(header, new byte[] {}, new HashMap<Object, Object>())).array();

        // compute the signature
        Map<Object, Object> footer = new HashMap<Object, Object>();
        footer.put(
                HeaderKey.AUTOREG_MAC,
                codec.hmac(HmacType.HMAC_MD5, "AIRVANTAGE".getBytes(Charsets.UTF_8), session.getCommunicationInfo()
                        .getM3daSharedKey().getBytes(Charsets.UTF_8), session.getPassNegoClientSalt(), payload));

        session.setNewPassword(newPassword);
        session.setPassNegoState(PasswordNegoState.WAIT_ACK);

        return new M3daEnvelope(Collections.emptyMap(), payload, footer);
    }

    /** receive the client ack, store the password, the negotiation is done */
    private void storeNewPassword(M3daEnvelope env, M3daSession session) {
        LOG.debug("negotiation step 3 : client aknowledgment");

        // check secured envelop signature
        M3daEnvelope securedEnv = checkSecurityAndDecode(env, session);

        Integer status = (Integer) securedEnv.getHeader().get(HeaderKey.STATUS);
        if (status == null || StatusCode.OK.getCode() != status.intValue()) {
            throw new PasswordNegotiationException("wrong status code");
        }

        // happy ! let's store the new password
        session.setPassNegoState(PasswordNegoState.DONE);
        session.getCommunicationInfo().setM3daCredential(session.getNewPassword());
        securityStore.storeNewPassword(session.getCommunicationId(), session.getNewPassword());
    }

    /** check envelope authentication and decode the inner payload */
    private M3daEnvelope checkSecurityAndDecode(M3daEnvelope env, M3daSession session)
            throws PasswordNegotiationException {
        checkMac(env, session);

        // decode the secured envelope
        try {
            final List<M3daEnvelope> decoded = new ArrayList<M3daEnvelope>();
            codec.createEnvelopeDecoder().decodeAndAccumulate(ByteBuffer.wrap(env.getPayload()),
                    new DecoderOutput<M3daEnvelope>() {
                        @Override
                        public void decoded(M3daEnvelope pdu) {
                            decoded.add(pdu);
                        }
                    });
            return decoded.get(0);
        } catch (DecoderException e) {
            throw new PasswordNegotiationException("invalid inner envelope", e);
        }

    }

    /** check if the envelope is correctly signed */
    private void checkMac(M3daEnvelope env, M3daSession session) throws PasswordNegotiationException {
        byte[] wantedMac;
        wantedMac = codec.hmac(HmacType.HMAC_MD5, session.getCommunicationId().getBytes(Charsets.UTF_8), session
                .getCommunicationInfo().getM3daSharedKey().getBytes(Charsets.UTF_8), session.getPassNegoServerSalt(),
                env.getPayload());
        ByteBuffer receivedMac = (ByteBuffer) env.getFooter().get(HeaderKey.AUTOREG_MAC);
        if (receivedMac == null) {
            throw new PasswordNegotiationException("no MAC in the message footer");
        }

        if (!Arrays.areEqual(wantedMac, receivedMac.array())) {
            throw new PasswordNegotiationException("invalid MAC");
        }
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
