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

import org.apache.commons.lang3.Validate;

/**
 * The different authentication algorithm type used by M3DA
 */
public enum HmacType {

	HMAC_MD5("MD5"), HMAC_SHA1("SHA-1");

	/** the digest algorithm used by this authentication scheme */
	private final String digest;

	private HmacType(final String digest) {
		this.digest = digest;
	}

	public String getDigest() {
		return digest;
	}

	/**
	 * Return the {@link HmacType} matching the given description
	 * 
	 * @param typeDesc
	 *            the hmac type description
	 * @return the matching hmac type
	 */
	public static HmacType getHmacType(String typeDesc) {
		Validate.notNull(typeDesc);

		String lcType = typeDesc.toLowerCase();

		if (lcType.equals("hmac-md5")) {
			return HMAC_MD5;
		} else if (lcType.equals("hmac-sha1")) {
			return HMAC_SHA1;
		} else {
			throw new IllegalArgumentException("unsupported hmac type description :" + typeDesc);
		}
	}
}
