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
package m3da.codec;

import java.io.InputStream;
import java.io.OutputStream;

import m3da.codec.dto.CipherAlgorithm;
import m3da.codec.dto.HmacType;

/**
 * A service for encoding, decoding M3DA messages.
 * 
 */
public interface M3daCodecService {

    /** The available cipher operations */
    public enum CipherMode {
        ENCRYPTION, DECRYPTION
    }

    /** The server name used for security computations */
    public static final String SERVER_NAME = "AIRVANTAGE";

    /**
     * Create a decoder for the M3DA envelope
     */
    EnvelopeDecoder createEnvelopeDecoder();

    /**
     * Create an encoder for the M3DA envelope
     */
    EnvelopeEncoder createEnvelopeEncoder();

    /**
     * Create decoder the envelope body
     */
    BysantDecoder createBodyDecoder();

    /**
     * Create encoder the envelope body
     */
    BysantEncoder createBodyEncoder();

    /**
     * Compute the HMAC of a body using the M3DA RFC-2104 like algorithm
     * 
     * @param algorithm the checksum algorithm to use (sha1,md5) for the HMAC
     * @param username the username
     * @param password the password
     * @param salt the salt (e.g. nonce)
     * @param messageBody the body to checksum
     * @return the HMAC checksum value
     */
    byte[] hmac(final HmacType algorithm, final byte[] username, final byte[] password, final byte[] salt,
            final byte[] messageBody);

    /**
     * Perform encryption or decryption on the data from a stream to another one.
     * <p>
     * The given password and nonce are used to compute the cipher key.
     * 
     * @param cipherMode encryption or decryption
     * @param algorithm the cryptographic algorithm to be used
     * @param password the password
     * @param nonce the nonce to use
     * @param content the content to be ciphered/deciphered
     * @param result the resulting content
     */
    void cipher(final CipherMode cipherMode, final CipherAlgorithm algorithm, final byte[] password,
            final byte[] nonce, final InputStream content, final OutputStream result);

}
