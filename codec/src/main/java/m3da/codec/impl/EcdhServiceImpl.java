package m3da.codec.impl;

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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import m3da.codec.EcdhService;
import m3da.codec.Hex;
import m3da.codec.M3daCodecServiceRuntimeException;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link EcdhService}
 */
public class EcdhServiceImpl implements EcdhService {

    private static final Logger LOG = LoggerFactory.getLogger(EcdhServiceImpl.class);

    /** To be used for generating Elliptic curve Diffie–Hellman public/private key pairs */
    private final KeyPairGenerator ecdhKeyGenerator;

    public EcdhServiceImpl() {

        this.registerSecurityProvider();

        // preare the ECDH key pair generator
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("P-521");
        try {
            ecdhKeyGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
            ecdhKeyGenerator.initialize(ecSpec, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "the used bouncycastle version should provide ECDH, bug ? check your dependencies", e);
        } catch (NoSuchProviderException e) {
            throw new IllegalStateException("bouncycastle should be provisioned, bug?", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("the code is probably broken", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyPair generateEcdhKeyPair() {
        LOG.debug("generateEcdhKeyPair");
        return ecdhKeyGenerator.generateKeyPair();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getPublicKeyCertificate(KeyPair aKeyPair) {
        LOG.debug("getKeyPubliccertificate");
        BCECPublicKey key = (BCECPublicKey) aKeyPair.getPublic();

        // full X.509 certificate
        byte[] fullCertificate = key.getEncoded();
        return Arrays.copyOfRange(fullCertificate, 25, fullCertificate.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] computeSharedSecret(KeyPair yourKeyPair, byte[] x963Cert) {
        LOG.debug("computeSharedSecret( peyPair = {}, x963Cert = {})", yourKeyPair, x963Cert);
        if (x963Cert[0] != 0x04) {
            throw new M3daCodecServiceRuntimeException("The certificate should start with 0x04");
        }
        if (x963Cert.length % 2 != 1) {
            throw new M3daCodecServiceRuntimeException("The certificate length should be odd");
        }

        int size = x963Cert.length / 2;

        // extract the two point coordinate
        ByteBuffer xBuff = ByteBuffer.allocate(size);
        xBuff.order(ByteOrder.BIG_ENDIAN);
        xBuff.put(x963Cert, 1, size);
        xBuff.flip();

        ByteBuffer yBuff = ByteBuffer.allocate(size);
        yBuff.order(ByteOrder.BIG_ENDIAN);
        yBuff.put(x963Cert, 1 + size, size);
        yBuff.flip();

        BCECPublicKey key = (BCECPublicKey) yourKeyPair.getPublic();
        BCECPrivateKey privKey = (BCECPrivateKey) yourKeyPair.getPrivate();

        // create point corresponding the the received public key
        ECFieldElement x = new ECFieldElement.Fp(((ECFieldElement.Fp) key.getQ().getX()).getQ(), new BigInteger(
                xBuff.array()));
        ECFieldElement y = new ECFieldElement.Fp(((ECFieldElement.Fp) key.getQ().getY()).getQ(), new BigInteger(
                yBuff.array()));

        ECPoint point = new ECPoint.Fp(key.getParameters().getCurve(), x, y);

        // compute the shared secret (ECDH magic)
        ECPoint P = point.multiply(privKey.getD());

        byte[] secret = leftPad(P.getX().toBigInteger().toByteArray(), 66);

        if (LOG.isDebugEnabled()) {
            LOG.debug("shared secret : {}", Hex.encodeHexString(secret));
        }

        return secret;
    }

    static final byte[] leftPad(byte[] src, int totalLength) {
        byte[] padded = new byte[totalLength];
        System.arraycopy(src, 0, padded, totalLength - src.length, src.length);
        return padded;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] cipherWithSecret(byte[] secret, byte[] payload) {
        byte[] xorKey = md5(secret);
        if (payload.length != xorKey.length) {
            throw new IllegalArgumentException("payload must be 16 bytes long");
        }
        for (int i = 0; i < payload.length; i++) {
            payload[i] ^= xorKey[i];
        }
        return payload;
    }

    /** Register BouncyCastle as JCE provider */
    private void registerSecurityProvider() {
        try {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
                LOG.info("Registration of BouncyCastle as a JCE provider succeeded");
            } else {
                LOG.warn("BouncyCastle already registered as a JCE provider");
            }
        } catch (Throwable t) {
            throw new M3daCodecServiceRuntimeException("Failed to register BouncyCastle as JCE provider", t);
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
