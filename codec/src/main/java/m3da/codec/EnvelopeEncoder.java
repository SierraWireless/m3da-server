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

import java.nio.ByteBuffer;

import m3da.codec.dto.M3daEnvelope;

/**
 * State-less M3DA encoder.
 */
public interface EnvelopeEncoder {

	/**
	 * Encode your M3DA envelope into a byte buffer
	 * 
	 * @param message
	 *            the envelope to encode
	 * @return the encoded byte buffer
	 */
	ByteBuffer encode(M3daEnvelope message);

}
