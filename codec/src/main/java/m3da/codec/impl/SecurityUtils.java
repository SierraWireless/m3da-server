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
package m3da.codec.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import m3da.codec.Hex;
import m3da.codec.M3daCodecServiceRuntimeException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for M3DA security
 */
public class SecurityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);

    private static final String BOUNCY_CASTLE = "BC";

    /**
     * extract of the RFC 2014
     * 
     * <pre>
     * 
     *    We define two fixed and different strings ipad and opad as follows
     *    (the 'i' and 'o' are mnemonics for inner and outer):
     * 
     *                   ipad = the byte 0x36 repeated B times
     *                   opad = the byte 0x5C repeated B times.
     * 
     *    To compute HMAC over the data `text' we perform
     * 
     *                     H(K XOR opad, H(K XOR ipad, text))
     * </pre>
     */
    private final byte[] opad;
    private final byte[] ipad;

    /** Block size (in bytes) when using SHA-1 or MD5 hash functions */
    private static final int BLOCK_SIZE = 64;

    public SecurityUtils() {

        opad = new byte[BLOCK_SIZE];
        ipad = new byte[BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            opad[i] = 0x5c;
            ipad[i] = 0x36;
        }

        this.registerSecurityProvider();
    }

    /**
     * HMAC(k, m) = H((k ⊕ opad) | H((k ⊕ ipad) | m))
     * 
     * @param algorithm the digest algorithm
     * @param k the key
     * @param m the message
     * @return the resulting hash value
     * @throws NoSuchAlgorithmException
     */
    public byte[] hmac(final String algorithm, final byte[] k, final byte[] m) throws NoSuchAlgorithmException {

        if (LOG.isTraceEnabled()) {
            LOG.trace("HMAC [digestAlgorithm={}, key={}, message={}]", new Object[] { algorithm,
                                    Hex.encodeHexString(k), Hex.encodeHexString(m) });
        }

        // the key should have the block size. padded with 0 if needed.
        byte[] key = Arrays.copyOf(k, BLOCK_SIZE);

        final MessageDigest digest = MessageDigest.getInstance(algorithm);

        digest.update(xor(key, ipad));
        digest.update(m);
        final byte[] rightPart = digest.digest();
        digest.reset();

        digest.update(xor(key, opad));
        return digest.digest(rightPart);
    }

    /**
     * Perform encryption or decryption on the data from a stream to another one.
     * 
     * @param encryptionMode <code>true</code> for encryption and <code>false</code> for decryption
     * @param algorithm the cryptographic algorithm to be used (e.g. <i>"AES"</i>)
     * @param transformation (e.g. <i>"AES/CBC/NoPadding"</i>)
     * @param key the cipher key
     * @param initialVector the initial vector
     * @param content the incoming stream
     * @param result the resulting stream
     */
    public void cipher(boolean encryptionMode, String algorithm, String transformation, byte[] key,
            byte[] initialVector, InputStream content, OutputStream result) throws GeneralSecurityException,
            IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace(
                    "cipher [encryption={}, algorithm={}, transformation={}, key={}, initialVector={}",
                    new Object[] { encryptionMode, algorithm, transformation, Hex.encodeHexString(key),
                                            Hex.encodeHexString(initialVector) });
        }

        SecretKey aesKey = new SecretKeySpec(key, algorithm);

        Cipher cipher = Cipher.getInstance(transformation, BOUNCY_CASTLE);
        cipher.init(encryptionMode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(
                initialVector));

        CipherOutputStream cpOs = new CipherOutputStream(result, cipher);

        int count;
        byte[] buffer = new byte[1024];

        while ((count = content.read(buffer)) > 0) {
            cpOs.write(buffer, 0, count);
        }

        try {
            cpOs.close();
        } catch (Exception ex) {
            // silent exception
        }
    }

    /** Register BouncyCastle as JCE provider */
    private void registerSecurityProvider() {
        try {
            if (Security.getProvider(BOUNCY_CASTLE) == null) {
                Security.addProvider(new BouncyCastleProvider());
                LOG.info("Registration of BouncyCastle as a JCE provider succeeded");
            } else {
                LOG.warn("BouncyCastle already registered as a JCE provider");
            }
        } catch (Throwable t) {
            throw new M3daCodecServiceRuntimeException("Failed to register BouncyCastle as JCE provider", t);
        }
    }

    /** xor two array of the same size into a new one */
    private byte[] xor(final byte[] a, final byte[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("cannot XOR arrays of different lengths");
        }
        final byte[] res = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            res[i] = (byte) (a[i] ^ b[i]);
        }
        return res;
    }

}
