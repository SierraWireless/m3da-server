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
package m3da.server.codec.impl;

import java.nio.ByteBuffer;

import m3da.server.codec.BysantDecoder;
import m3da.server.codec.DecoderException;
import m3da.server.codec.DecoderOutput;
import m3da.server.codec.EnvelopeDecoder;
import m3da.server.codec.dto.M3daEnvelope;
import m3da.server.codec.dto.M3daPdu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link EnvelopeDecoder}. Not thread safe for performance reason : instantiate one for each thread/session/device and push the
 * byte buffer as they come.
 * 
 */
public class EnvelopeDecoderImpl implements EnvelopeDecoder {

	private static final Logger LOG = LoggerFactory.getLogger(EnvelopeDecoderImpl.class);

	private final BysantDecoder enveloppeDecoder = new BysantDecoderImpl();

	private M3daEnvelope decodedEnvelope = null;

	private boolean someParasite = false;
	private Object parasite = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decodeAndAccumulate(ByteBuffer buffer, final DecoderOutput<M3daEnvelope> output) throws DecoderException {
		if (decodedEnvelope != null) {
			throw new IllegalStateException("already used decoder, instanciate a new one for your session");
		}
		parasite = null;
		// decode the envelope
		try {
			enveloppeDecoder.decodeAndAccumulate(buffer, new DecoderOutput<M3daPdu>() {

				/**
				 * {@inheritDoc}
				 */
				@Override
				public void decoded(M3daPdu pdu) {
					if (pdu instanceof M3daEnvelope) {
						decodedEnvelope = (M3daEnvelope) pdu;
					} else {
						decodedEnvelope = null;
						parasite = pdu;
						someParasite = true;
					}
				}
			});
		} catch (ClassCastException e) {
			throw new DecoderException("could not decode the envelope", e);
		}
		if (parasite != null) {
			throw new DecoderException("no envelope found in this message, but a : " + parasite.getClass().getCanonicalName());
		}
		// decode the envelope content
		if (decodedEnvelope != null) {
			output.decoded(decodedEnvelope);
			decodedEnvelope = null;
		} else {
			LOG.debug("accumulating more bytes");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finishDecode() throws DecoderException {
		enveloppeDecoder.finishDecode();
		if (someParasite) {
			throw new DecoderException("trailling data : " + parasite);
		}
	}
}
