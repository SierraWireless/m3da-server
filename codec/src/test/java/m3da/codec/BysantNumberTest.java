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
import java.util.List;
import java.util.Map;

import m3da.codec.DecoderException;
import m3da.codec.DecoderOutput;
import m3da.codec.impl.BysantContext;
import m3da.codec.impl.BysantDecoderImpl;
import m3da.codec.impl.BysantEncoderImpl;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * samples.put(@link BysantEncoder} unit test
 */
public class BysantNumberTest {
	private static final Logger LOG = LoggerFactory.getLogger(BysantNumberTest.class);

	private final Map<Number, String> globalSamples = Maps.newHashMap();
	{
		globalSamples.put(-2147483649L, "fdffffffff7fffffff");
		globalSamples.put(-2147483648, "fc80000000");
		globalSamples.put(-33818656, "fcfdfbf7e0");
		globalSamples.put(-33818655, "fbffffff");
		globalSamples.put(-264224, "fa000000");
		globalSamples.put(-264223, "f7ffff");
		globalSamples.put(-2080, "f40000");
		globalSamples.put(-2079, "efff");
		globalSamples.put(-32, "e800");
		globalSamples.put(-31, "80");
		globalSamples.put(0, "9f");
		globalSamples.put(64, "df");
		globalSamples.put(65, "e000");
		globalSamples.put(2112, "e7ff");
		globalSamples.put(2113, "f00000");
		globalSamples.put(264256, "f3ffff");
		globalSamples.put(264257, "f8000000");
		globalSamples.put(33818688, "f9ffffff");
		globalSamples.put(33818689, "fc02040841");
		globalSamples.put(2147483647, "fc7fffffff");
		globalSamples.put(2147483648L, "fd0000000080000000");
		globalSamples.put(0.25f, "fe3e800000");
		globalSamples.put(1e30 / 13, "ff45ef11a8e476d4d1");
	}

	private final Map<Number, String> numberSamples = Maps.newHashMap();
	{
		numberSamples.put(-2147483649l, "fdffffffff7fffffff");
		numberSamples.put(-2147483648, "fc80000000");
		numberSamples.put(-67637346, "fcfbf7ef9e");

		numberSamples.put(-67637345, "fbffffff");
		numberSamples.put(-528482, "f8000000");
		numberSamples.put(-528481, "f3ffff");
		numberSamples.put(-4194, "ec0000");
		numberSamples.put(-4193, "e3ff");
		numberSamples.put(-98, "d400");
		numberSamples.put(-97, "01");
		numberSamples.put(0, "62");
		numberSamples.put(97, "c3");
		numberSamples.put(98, "c400");
		numberSamples.put(4193, "d3ff");
		numberSamples.put(4194, "e40000");
		numberSamples.put(528481, "ebffff");
		numberSamples.put(528482, "f4000000");
		numberSamples.put(67637345, "f7ffffff");

		numberSamples.put(67637346, "fc04081062");
		numberSamples.put(2147483647, "fc7fffffff");
		numberSamples.put(2147483648l, "fd0000000080000000");
		numberSamples.put(0.25f, "fe3e800000");
		numberSamples.put(1e30 / 13, "ff45ef11a8e476d4d1");
	}

	private final Map<Number, String> uisSamples = Maps.newHashMap();
	{
		uisSamples.put(0, "3b");
		uisSamples.put(139, "c6");
		uisSamples.put(140, "c700");
		uisSamples.put(8331, "e6ff");
		uisSamples.put(8332, "e70000");
		uisSamples.put(1056907, "f6ffff");
		uisSamples.put(1056908, "f7000000");
		uisSamples.put(135274635, "feffffff");
		uisSamples.put(135274636L, "ff0810208c");
		uisSamples.put(4294967295L, "ffffffffff");
	}

	@Test
	public void decode_global_number() throws DecoderException, org.apache.commons.codec.DecoderException {

		Map<Number, String> samples = globalSamples;
		BysantContext ctx = BysantContext.GLOBAL;

		testDecode(samples, ctx);
	}

	@Test
	public void decode_number_number() throws DecoderException, org.apache.commons.codec.DecoderException {

		Map<Number, String> samples = numberSamples;
		BysantContext ctx = BysantContext.NUMBERS;

		testDecode(samples, ctx);
	}

	@Test
	public void decode_uis_number() throws DecoderException, org.apache.commons.codec.DecoderException {

		Map<Number, String> samples = uisSamples;
		BysantContext ctx = BysantContext.UINTS_AND_STRS;
		testDecode(samples, ctx);
	}

	private void testDecode(Map<Number, String> samples, BysantContext ctx) throws DecoderException, org.apache.commons.codec.DecoderException {
		for (Map.Entry<Number, String> entry : samples.entrySet()) {
			BysantDecoderImpl decoder = new BysantDecoderImpl();
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" decoding number : " + entry.getValue() + " => " + entry.getKey());
			LOG.debug(" ");

			ListDecoder output = new ListDecoder();
			decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(entry.getValue().toCharArray())), output, ctx);
			Assert.assertEquals(1, output.list.size());

			Assert.assertEquals(entry.getKey(), output.list.get(0));

		}
	}

	@Test
	public void encode_global_number() {

		BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (Map.Entry<Number, String> entry : globalSamples.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding number : " + entry.getKey());
			LOG.debug(" ");
			ByteBuffer res = encoder.encode(BysantContext.GLOBAL, new Object[] { entry.getKey() });
			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));

		}
	}

	@Test
	public void encode_numbers_number() {

		BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (Map.Entry<Number, String> entry : numberSamples.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding number : " + entry.getKey());
			LOG.debug(" ");
			ByteBuffer res = encoder.encode(BysantContext.NUMBERS, new Object[] { entry.getKey() });

			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));

		}
	}

	@Test
	public void encode_uis_number() {

		BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (Map.Entry<Number, String> entry : uisSamples.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding number : " + entry.getKey());
			LOG.debug(" ");
			ByteBuffer res = encoder.encode(BysantContext.UINTS_AND_STRS, new Object[] { entry.getKey() });
			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));
		}
	}

	private static class ListDecoder implements DecoderOutput<Object> {
		public final List<Object> list = Lists.newArrayList();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void decoded(Object pdu) {
			list.add(pdu);
		}
	}

}
