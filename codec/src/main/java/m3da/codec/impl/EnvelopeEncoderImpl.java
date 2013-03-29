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
package m3da.codec.impl;

import java.nio.ByteBuffer;

import m3da.codec.BysantEncoder;
import m3da.codec.EnvelopeEncoder;
import m3da.codec.dto.M3daEnvelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link EnvelopeEncoder}. Stateless : encode one {@link M3daEnvelope} into a big {@link ByteBuffer}
 */
public class EnvelopeEncoderImpl implements EnvelopeEncoder {

	private static final Logger LOG = LoggerFactory.getLogger(EnvelopeEncoderImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer encode(M3daEnvelope envelope) {
		LOG.debug("encode(envelope = {})", envelope);
		// for encoding the envelope elements
		BysantEncoder encoder = new BysantEncoderImpl();
		return encoder.encode(envelope);
	}
}
