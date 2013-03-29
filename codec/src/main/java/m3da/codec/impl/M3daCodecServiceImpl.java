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

import m3da.codec.BysantDecoder;
import m3da.codec.BysantEncoder;
import m3da.codec.EnvelopeDecoder;
import m3da.codec.EnvelopeEncoder;
import m3da.codec.M3daCodecService;

/**
 * Implementation of {@link M3daCodecService}
 */
public class M3daCodecServiceImpl implements M3daCodecService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EnvelopeDecoder createEnvelopeDecoder() {
		return new EnvelopeDecoderImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EnvelopeEncoder createEnvelopeEncoder() {
		return new EnvelopeEncoderImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BysantDecoder createBodyDecoder() {
		return new BysantDecoderImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BysantEncoder createBodyEncoder() {
		return new BysantEncoderImpl();
	}

}
