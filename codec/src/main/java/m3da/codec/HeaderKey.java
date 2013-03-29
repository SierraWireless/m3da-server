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
 * Constant for the various M3DA Header values
 */
public interface HeaderKey {

	/** the client identifier */
	public static final String ID = "id";

	/** the last package status code */
	public static final String STATUS = "status";

	/** the nonce to be used for the next message */
	public static final String NONCE = "nonce";

	/** challenge the other peer for authentication */
	public static final String CHALLENGE = "challenge";

	/** the H-MAC signature (footer) */
	public static final String MAC = "mac";

	/** Header used during credential generation for sending salt to the remote side */
	public static final String AUTOREG_SALT = "autoreg_salt";

	/** Header used during credential generation for sending ECCDH public key to the remote side */
	public static final String AUTOREG_PUBKEY = "autoreg_pubkey";

	/** Header used during credential generation for sending ECCDH ciphered value */
	public static final String AUTOREG_CTEXT = "autoreg_ctext";

	/** Footer used during credential generation for signing messages */
	public static final String AUTOREG_MAC = "autoreg_mac";
}
