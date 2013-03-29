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

/**
 * AWT-DA 3 status codes
 * 
 */
public enum StatusCode {

	/** Everything went fine */
	OK(200),

	/** Server side unexpected error */
	UNEXPECTED_ERROR(500),
	/** Server can't answer to the request, try later (e.g. : quota exceeded) */
	SERVICE_UNAVAIBLE(503),
	/** Malformed request */
	BAD_REQUEST(400),
	/** Unauthorized (usually happens when the credentials are not correct) */
	UNAUTHORIZED(401),
	/** This system is not allowed to communicate, authentication will not help */
	FORBIDDEN(403),
	/** Authentication required (usually happens when no credentials were provided) */
	AUTHENTICATION_REQUIRED(407),

	/**
	 * The payload data need to be encrypted (usually happens when a peer try to send data without encryption and the other require the data to be
	 * encrypted)
	 */
	ENCRYPTION_NEEDED(450),
	/** the shortcut map version is not compatible (anyway we don't support this feature) */
	SHORTCUT_MAP_ERROR(451);

	private final int code;

	private StatusCode(final int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
