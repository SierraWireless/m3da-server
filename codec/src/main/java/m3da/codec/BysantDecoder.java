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

import m3da.codec.dto.M3daPdu;
import m3da.codec.impl.BysantContext;

/**
 * A state-full accumulating bysant stream decoder.
 */
public interface BysantDecoder {

	/**
	 * Decode some buffer and output the decoded object in a {@link DecoderOutput}. If some bytes are remaining not decoded, we accumulate them and
	 * decode them during the next call to decode.
	 * 
	 * @param buffer
	 *            the input buffer to consume for decoding
	 * @param output
	 *            the output callback , called each time an object is successfully decoded
	 * @throws DecoderException
	 *             if some chunk is malformed
	 */
	void decodeAndAccumulate(ByteBuffer buffer, DecoderOutput<M3daPdu> output) throws DecoderException;

	/**
	 * To be called once all the bytes where pushed using the {@link #decode(ByteBuffer, DecoderOutput, BysantContext)} method.
	 * 
	 * @throws DecoderException
	 *             If some bytes remains undecoded.
	 */
	void finishDecode() throws DecoderException;

}
