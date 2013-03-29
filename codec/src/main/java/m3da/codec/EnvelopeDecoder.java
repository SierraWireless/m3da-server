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
 * State-full M3DA decoder.
 */
public interface EnvelopeDecoder {

	/**
	 * Call it for decoding each received ByteBuffer. If the envelope is split in multiple TCP packet the decoder will accumulate the ByteBuffers
	 * until the complete envelope can be decoded. Once the envelope is decoded it will be pushed to the decoderOutput
	 * 
	 * @param buffer
	 *            the received buffer of the message
	 * @param decoderOutput
	 *            callback called if the envelope is decoded
	 * @throws DecoderException
	 */
	void decodeAndAccumulate(ByteBuffer buffer, DecoderOutput<M3daEnvelope> decoderOutput) throws DecoderException;

	/**
	 * finish decode, if some bytes are remaining undecodable some exception will be thrown
	 */
	void finishDecode() throws DecoderException;
}
