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
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Unit test of List encoding in various encoding context
 */
public class BysantListTest {

	private static final Logger LOG = LoggerFactory.getLogger(BysantListTest.class);

	private ByteBuffer toBB(final String s) {
		return ByteBuffer.wrap(s.getBytes(Charsets.UTF_8));
	}

	private final Map<List<?>, String> globalSamples = Maps.newLinkedHashMap();
	{
		globalSamples.put(null, "00");
		globalSamples.put(Lists.newArrayList(toBB("one")), "2b066f6e65");
		globalSamples.put(Lists.newArrayList(toBB("one"), toBB("two"), 3, 4, 5, 6, 7, 8, 9), "33066f6e650674776fa2a3a4a5a6a7a8");
		globalSamples.put(Lists.newArrayList(toBB("one"), toBB("two"), 3, 4, 5, 6, 7, 8, 9, toBB("ten")),
				"343b066f6e650674776fa2a3a4a5a6a7a80674656e");
	}

	private final Map<List<?>, String> listAndMapSamples = Maps.newLinkedHashMap();
	{
		listAndMapSamples.put(null, "00");
		listAndMapSamples.put(Lists.newArrayList(toBB("one")), "02066f6e65");
		listAndMapSamples.put(Lists.newArrayList(toBB("one"), toBB("two"), 3, 4, 5, 6, 7, 8, 9), "0a066f6e650674776fa2a3a4a5a6a7a8");
		listAndMapSamples.put(Lists.newArrayList(toBB("one"), toBB("two"), 3, 4, 5, 6, 7, 8, 9, toBB("ten")),
				"0b066f6e650674776fa2a3a4a5a6a7a80674656e");
	}

	@Test
	public void encode_global_list() {
		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (final Map.Entry<List<?>, String> entry : globalSamples.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding List : " + entry.getKey());
			LOG.debug(" ");
			final ByteBuffer res = encoder.encode(BysantContext.GLOBAL, new Object[] { entry.getKey() });
			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));
		}
	}

	@Test
	public void encode_list_and_map_list() {
		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (final Map.Entry<List<?>, String> entry : listAndMapSamples.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding List : " + entry.getKey());
			LOG.debug(" ");
			final ByteBuffer res = encoder.encode(BysantContext.LIST_AND_MAPS, new Object[] { entry.getKey() });
			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));
		}
	}

	@Test
	public void decode_global_list() throws DecoderException, org.apache.commons.codec.DecoderException {
		testDecode(globalSamples, BysantContext.GLOBAL);
	}

	@Test
	public void decode_list_and_map_list() throws DecoderException, org.apache.commons.codec.DecoderException {
		testDecode(listAndMapSamples, BysantContext.LIST_AND_MAPS);
	}

	private void testDecode(final Map<List<?>, String> samples, final BysantContext ctx) throws DecoderException,
			org.apache.commons.codec.DecoderException {
		for (final Map.Entry<List<?>, String> entry : samples.entrySet()) {
			final BysantDecoderImpl decoder = new BysantDecoderImpl();
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" decoding list : " + entry.getValue() + " => " + entry.getKey());
			LOG.debug(" ");

			final ListDecoder output = new ListDecoder();
			decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(entry.getValue().toCharArray())), output, ctx);
			decoder.finishDecode();
			Assert.assertEquals(1, output.list.size());

			Assert.assertEquals(entry.getKey(), output.list.get(0));

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
