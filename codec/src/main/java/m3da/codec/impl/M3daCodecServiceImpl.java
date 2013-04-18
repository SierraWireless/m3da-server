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
import java.util.Arrays;

import m3da.codec.BysantDecoder;
import m3da.codec.BysantEncoder;
import m3da.codec.EnvelopeDecoder;
import m3da.codec.EnvelopeEncoder;
import m3da.codec.Hex;
import m3da.codec.M3daCodecService;
import m3da.codec.M3daCodecServiceRuntimeException;
import m3da.codec.dto.CipherAlgorithm;
import m3da.codec.dto.HmacType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link M3daCodecService}
 */
public class M3daCodecServiceImpl implements M3daCodecService {

    private static final Logger LOG = LoggerFactory.getLogger(M3daCodecServiceImpl.class);

    private SecurityUtils securityUtils = new SecurityUtils();

    /**
     * {@inheritDoc}
     */
    @Override
    public EnvelopeDecoder createEnvelopeDecoder() {
        return new EnvelopeDecoderImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnvelopeEncoder createEnvelopeEncoder() {
        return new EnvelopeEncoderImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BysantDecoder createBodyDecoder() {
        return new BysantDecoderImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BysantEncoder createBodyEncoder() {
        return new BysantEncoderImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] hmac(final HmacType algorithm, final byte[] username, final byte[] password, final byte[] salt,
            final byte[] messageBody) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "HMAC [algorithm={}, username={}, password={}, nonce={}, body={}]",
                    new Object[] { algorithm, Hex.encodeHexString(username), Hex.encodeHexString(password),
                                            Hex.encodeHexString(salt), Hex.encodeHexString(messageBody) });
        }

        // K = HMD5(username | HMD5(password))
        byte[] md5Pwd = md5(password);
        final byte[] k = md5(concat(username, md5Pwd));

        // m = protectedEnveloppe | nonce
        final byte[] m = concat(messageBody, salt);

        try {
            return securityUtils.hmac(algorithm.getDigest(), k, m);
        } catch (NoSuchAlgorithmException e) {
            // should never happen (only if you use a really broken JVM implementation
            throw new IllegalStateException("missing MD5 or SHA-1 in the JVM", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cipher(final CipherMode cipherMode, final CipherAlgorithm algorithm, final byte[] password,
            final byte[] nonce, InputStream content, OutputStream result) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("cipher [cipherMode={}, algorithm={}, password={}, nonce={}", new Object[] { cipherMode,
                                    algorithm, Hex.encodeHexString(password), Hex.encodeHexString(nonce) });
        }

        try {
            byte[] k = md5(password);
            byte[] m = nonce;
            byte[] key = securityUtils.hmac(HmacType.HMAC_MD5.getDigest(), k, m);

            int expectedKeyLength = algorithm.getKeyLength();

            if (key.length < expectedKeyLength) {
                LOG.trace("extending cipher key, expected length : {}, actual length : {}", expectedKeyLength,
                        key.length);

                // another hash is concatenated to extend the key length
                byte[] key2 = securityUtils.hmac(HmacType.HMAC_MD5.getDigest(), k, concat(m, m));
                key = concat(key, key2);
            }

            // truncate the key to the expected length
            byte[] finalKey = Arrays.copyOf(key, expectedKeyLength);

            // the initial vector is set equal to the hash of the current nonce.
            byte[] initialVector = md5(nonce);

            securityUtils.cipher(cipherMode.equals(CipherMode.ENCRYPTION), algorithm.getAlgorithm(),
                    algorithm.getTransformation(), finalKey, initialVector, content, result);
        } catch (NoSuchAlgorithmException e) {
            throw new M3daCodecServiceRuntimeException("unexpected error while ciphering a m3da content", e);
        } catch (GeneralSecurityException e) {
            throw new M3daCodecServiceRuntimeException("unexpected security error while ciphering a m3da content", e);
        } catch (IOException e) {
            throw new M3daCodecServiceRuntimeException("unexpected I/O error while ciphering a m3da content", e);
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

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
