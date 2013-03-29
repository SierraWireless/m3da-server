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
 * A service for encoding, decoding M3DA messages.
 * 
 */
public interface M3daCodecService {

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

}
