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
package m3da.server.codec;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.List;

import m3da.server.codec.impl.BysantContext;
import m3da.server.codec.impl.BysantDecoderImpl;
import m3da.server.codec.impl.BysantEncoderImpl;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class BysantBooleanTest {

	@Test
	public void encode_true_boolean() {
		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		final ByteBuffer res = encoder.encode(BysantContext.GLOBAL, new Object[] { true });
		assertEquals(1, res.array().length);
		assertEquals((byte) 0x01, res.array()[0]);
	}

	@Test
	public void encode_false_boolean() {
		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		final ByteBuffer res = encoder.encode(BysantContext.GLOBAL, new Object[] { false });
		assertEquals(1, res.array().length);
		assertEquals((byte) 0x02, res.array()[0]);
	}

	@Test
	public void decode_true_boolean() throws DecoderException {
		final BysantDecoderImpl decoder = new BysantDecoderImpl();

		ListDecoder output = new ListDecoder();
		decoder.decodeAndAccumulate(ByteBuffer.wrap(new byte[] { 0x01 }), output, BysantContext.GLOBAL);
		Assert.assertEquals(1, output.list.size());

		Assert.assertEquals(true, output.list.get(0));
	}

	@Test
	public void decode_false_boolean() throws DecoderException {
		final BysantDecoderImpl decoder = new BysantDecoderImpl();

		ListDecoder output = new ListDecoder();
		decoder.decodeAndAccumulate(ByteBuffer.wrap(new byte[] { 0x02 }), output, BysantContext.GLOBAL);
		Assert.assertEquals(1, output.list.size());

		Assert.assertEquals(false, output.list.get(0));
	}

	private static class ListDecoder implements DecoderOutput<Object> {
		public final List<Object> list = Lists.newArrayList();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void decoded(final Object pdu) {
			list.add(pdu);
		}
	}

}
