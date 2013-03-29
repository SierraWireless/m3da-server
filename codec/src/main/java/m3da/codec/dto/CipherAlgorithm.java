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
package m3da.codec.dto;

import javax.crypto.Cipher;

/**
 * The algorithms to be used for M3DA encryption.
 * <p>
 * Encryption for M3DA only uses AES (Advanced Encryption Standard) as cryptographic algorithm.
 */
public enum CipherAlgorithm {

	/** AES Cipher-block chaining with a 128-bit key and PKCS5 padding */
	AES_CBC_128("AES/CBC/PKCS5Padding", 16),

	/** AES Cipher-block chaining with a 256-bit key and PKCS5 padding */
	AES_CBC_256("AES/CBC/PKCS5Padding", 32),

	/** AES Counter mode with a 128-bit key (no padding required) */
	AES_CTR_128("AES/CTR/NoPadding", 16),

	/** AES Counter mode with a 256-bit key (no padding required) */
	AES_CTR_256("AES/CTR/NoPadding", 32);

	private final String transformation;
	private final int keyLength;

	private CipherAlgorithm(final String transformation, final int keyLength) {
		this.transformation = transformation;
		this.keyLength = keyLength;
	}

	/**
	 * @return the transformation that must be implemented by the {@link Cipher}.
	 */
	public String getTransformation() {
		return transformation;
	}

	/** @return the cipher key length in bytes */
	public int getKeyLength() {
		return keyLength;
	}

	/** the cryptographic algorithm for the cipher */
	public String getAlgorithm() {
		return "AES";
	}

	/**
	 * Return the {@link CipherAlgorithm} matching the given description
	 * 
	 * @param cipherDesc
	 *            the cipher description
	 * @return the matching cipher
	 */
	public static CipherAlgorithm getCipher(String cipherDesc) {
		String lcCipher = cipherDesc.toLowerCase();

		if (lcCipher.equals("aes-cbc-128")) {
			return AES_CBC_128;
		} else if (lcCipher.equals("aes-cbc-256")) {
			return AES_CBC_256;
		} else if (lcCipher.equals("aes-ctr-128")) {
			return AES_CTR_128;
		} else if (lcCipher.equals("aes-ctr-256")) {
			return AES_CTR_256;
		} else {
			throw new IllegalArgumentException("unsupported cipher description :" + cipherDesc);
		}
	}

}
