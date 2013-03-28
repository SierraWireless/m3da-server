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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import m3da.server.codec.impl.BysantContext;
import m3da.server.codec.impl.BysantDecoderImpl;
import m3da.server.codec.impl.BysantEncoderImpl;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Unit test for different context encoding Strings
 */
public class BysantStringTest {

	private static final Logger LOG = LoggerFactory.getLogger(BysantStringTest.class);

	private final Map<String, String> globalStrings = Maps.newHashMap();
	{
		globalStrings.put("", "03");
		globalStrings.put(StringUtils.repeat("a", 32), "23" + StringUtils.repeat("61", 32));
		globalStrings.put(StringUtils.repeat("a", 33), "2400" + StringUtils.repeat("61", 33));
		globalStrings.put(StringUtils.repeat("a", 1056), "27ff" + StringUtils.repeat("61", 1056));
		globalStrings.put(StringUtils.repeat("a", 1057), "280000" + StringUtils.repeat("61", 1057));
		globalStrings.put(StringUtils.repeat("a", 66592), "28ffff" + StringUtils.repeat("61", 66592));
		globalStrings.put(StringUtils.repeat("a", 66593), "29ffff" + StringUtils.repeat("61", 65535) + "0422" + StringUtils.repeat("61", 1058)
				+ "0000");
	}

	private final Map<String, String> uisStrings = Maps.newHashMap();
	{
		uisStrings.put("", "01");
		uisStrings.put(StringUtils.repeat("a", 47), "30" + StringUtils.repeat("61", 47));
		uisStrings.put(StringUtils.repeat("a", 48), "3100" + StringUtils.repeat("61", 48));
		uisStrings.put(StringUtils.repeat("a", 2095), "38ff" + StringUtils.repeat("61", 2095));
		uisStrings.put(StringUtils.repeat("a", 2096), "390000" + StringUtils.repeat("61", 2096));
		uisStrings.put(StringUtils.repeat("a", 67631), "39ffff" + StringUtils.repeat("61", 67631));
		uisStrings.put(StringUtils.repeat("a", 67632), "3affff" + StringUtils.repeat("61", 65535) + "0831" + StringUtils.repeat("61", 2097) + "0000");
	}

	@Test
	public void decode_global_string() throws DecoderException, org.apache.commons.codec.DecoderException {

		final Map<String, String> samples = globalStrings;
		final BysantContext ctx = BysantContext.GLOBAL;

		testDecode(samples, ctx);
	}

	@Test
	public void decode_uis_string() throws DecoderException, org.apache.commons.codec.DecoderException {

		final Map<String, String> samples = uisStrings;
		final BysantContext ctx = BysantContext.UINTS_AND_STRS;

		testDecode(samples, ctx);
	}

	@Test
	public void encode_global_string() {

		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (final Map.Entry<String, String> entry : globalStrings.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding String : " + entry.getKey());
			LOG.debug(" ");
			final ByteBuffer res = encoder.encode(BysantContext.GLOBAL, new Object[] { entry.getKey() });
			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));
		}
	}

	@Test
	public void encode_uis_string() {

		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (final Map.Entry<String, String> entry : uisStrings.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding String : " + entry.getKey());
			LOG.debug(" ");
			final ByteBuffer res = encoder.encode(BysantContext.UINTS_AND_STRS, new Object[] { entry.getKey() });

			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));

		}
	}

	private void testDecode(final Map<String, String> samples, final BysantContext ctx) throws DecoderException,
			org.apache.commons.codec.DecoderException {
		for (final Map.Entry<String, String> entry : samples.entrySet()) {
			final ListDecoder output = new ListDecoder();
			final BysantDecoderImpl decoder = new BysantDecoderImpl();
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" decoding String : " + entry.getValue() + " => " + entry.getKey());
			LOG.debug(" ");

			decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(entry.getValue().toCharArray())), output, ctx);

			Assert.assertEquals(1, output.list.size());

			if (entry.getKey() == null) {
				Assert.assertNull(output.list.get(0));
			} else {
				final ByteBuffer res = (ByteBuffer) output.list.get(0);
				final String strRes = new String(res.array(), Charsets.UTF_8);
				Assert.assertEquals(entry.getKey(), strRes);
			}
		}
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
