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
 * Encode/Decode Map
 */
public class BysantMapTest {

	private static final Logger LOG = LoggerFactory.getLogger(BysantMapTest.class);

	private final Map<Map<?, ?>, String> globalSamples = Maps.newLinkedHashMap();
	{
		globalSamples.put(null, "00");
		globalSamples.put(build_map(), "41");
		globalSamples.put(build_map(StringUtils.repeat("a", 50), ByteBuffer.wrap("onecharmap".getBytes(Charsets.UTF_8))),
				"42310261616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161610d6f6e65636861726d6170");
		globalSamples
				.put(build_map("1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6, "7", 7, "8", 8, "9", 9, "10", 10, "11", 11, "12", 12, "13", 13, "14",
						14, "15", 15, "16", 16, "17", 17, "18", 18, "19", 19, 20, 20),
						"4b450231a00232a10233a20234a30235a40236a50237a60238a70239a8033130a9033131aa033132ab033133ac033134ad033135ae033136af033137b0033138b1033139b24fb3");
	}

	private final Map<Map<?, ?>, String> mapAndListSamples = Maps.newLinkedHashMap();
	{
		mapAndListSamples.put(null, "00");
		mapAndListSamples.put(build_map(), "83");
		mapAndListSamples.put(build_map(StringUtils.repeat("a", 50), ByteBuffer.wrap("onecharmap".getBytes(Charsets.UTF_8))),
				"84310261616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161610d6f6e65636861726d6170");

		final Map<Object, Object> largeMap = Maps.newLinkedHashMap();

		for (int i = 0; i < 100; i++) {
			largeMap.put(i, ByteBuffer.wrap(String.valueOf(i).getBytes(Charsets.UTF_8)));
		}

		mapAndListSamples
				.put(largeMap,
						"c0623b04303c04313d04323e04333f043440043541043642043743043844043945053130460531314705313248053133490531344a0531354b0531364c0531374d"
								+ "0531384e0531394f053230500532315105323252053233530532345405323555053236560532375705323858053239590533305a0533315b0533325c0533335d05"
								+ "33345e0533355f053336600533376105333862053339630534306405343165053432660534336705343468053435690534366a0534376b0534386c0534396d0535"
								+ "306e0535316f053532700535337105353472053535730535367405353775053538760535397705363078053631790536327a0536337b0536347c0536357d053636"
								+ "7e0536377f053638800536398105373082053731830537328405373385053734860537358705373688053737890537388a0537398b0538308c0538318d0538328e"
								+ "0538338f053834900538359105383692053837930538389405383995053930960539319705393298053933990539349a0539359b0539369c0539379d0539389e053939");
	}

	@Test
	public void encode_global_map() {
		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (final Map.Entry<Map<?, ?>, String> entry : globalSamples.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding map : " + entry.getKey());
			LOG.debug(" ");
			final ByteBuffer res = encoder.encode(BysantContext.GLOBAL, new Object[] { entry.getKey() });
			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));
		}
	}

	@Test
	public void encode_list_and_map_map() {
		final BysantEncoderImpl encoder = new BysantEncoderImpl();

		for (final Map.Entry<Map<?, ?>, String> entry : mapAndListSamples.entrySet()) {
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");
			LOG.debug(" encoding map : " + entry.getKey());
			LOG.debug(" ");
			final ByteBuffer res = encoder.encode(BysantContext.LIST_AND_MAPS, new Object[] { entry.getKey() });
			Assert.assertEquals(entry.getValue(), Hex.encodeHexString(res.array()));
		}
	}

	@Test
	public void decode_global_map() throws DecoderException, org.apache.commons.codec.DecoderException {
		testDecode(globalSamples, BysantContext.GLOBAL);
	}

	@Test
	public void decode_list_and_map_map() throws DecoderException, org.apache.commons.codec.DecoderException {
		testDecode(mapAndListSamples, BysantContext.LIST_AND_MAPS);
	}

	private void testDecode(final Map<Map<?, ?>, String> samples, final BysantContext ctx) throws DecoderException,
			org.apache.commons.codec.DecoderException {
		for (final Map.Entry<Map<?, ?>, String> entry : samples.entrySet()) {
			final BysantDecoderImpl decoder = new BysantDecoderImpl();
			LOG.debug(" ");
			LOG.debug("----------------------------------------------------------");
			LOG.debug(" ");

			LOG.debug(" decoding map : " + entry.getValue() + " => " + entry.getKey());
			LOG.debug(" ");

			final ListDecoder output = new ListDecoder();
			decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(entry.getValue().toCharArray())), output, ctx);
			decoder.finishDecode();
			Assert.assertEquals(1, output.list.size());

			if (entry.getKey() == null) {
				Assert.assertNull(output.list.get(0));
			} else {
				Assert.assertEquals(entry.getKey(), output.list.get(0));
			}

		}
	}

	private Map<Object, Object> build_map(final Object... entries) {
		final Map<Object, Object> res = Maps.newLinkedHashMap();
		if (entries.length % 2 != 0) {
			throw new IllegalArgumentException("odd number of elements");
		}
		for (int i = 0; i < entries.length / 2; i++) {
			res.put(entries[i * 2], entries[i * 2 + 1]);
		}
		return res;
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
