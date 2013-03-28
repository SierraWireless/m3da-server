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

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import m3da.server.codec.dto.M3daEnvelope;
import m3da.server.codec.dto.M3daMessage;
import m3da.server.codec.dto.M3daPdu;
import m3da.server.codec.impl.BysantDecoderImpl;
import m3da.server.codec.impl.EnvelopeDecoderImpl;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Unit test for {@link EnvelopeDecoderImpl}
 */
public class M3daDecoderImplTest {

	private static final Logger LOG = LoggerFactory.getLogger(M3daDecoderImplTest.class);

	private ByteBuffer toBB(final String s) {
		try {
			return ByteBuffer.wrap(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void decode_a_multi_dimension_awt_message() throws Exception {

		// prepare
		final String chunk1 = "6085036964f1d9ff04646576123132333435363738394142";
		final String chunk2 = "43444546317761126d7941737365742e736f6d65776865726547843148412e422e432e442e452e462e472e";
		final String chunk3 = "482e492e4a2e4b2e4c2e4d2e4f2e502e512e522e532e542e552e562e572e582e592e5a2e412e422e432e442e452e462e472e482e492e4a2e4b2e4c2e4d2e4f2e502e512e522e532e542e552e562e572e582e592e5a2e7269646963756c6f75736c796c6f";
		final String chunk4 = "6e67706174682f09737472696e67abf4bb30fc1ad274802d04410442e03a83";

		final EnvelopeDecoder decoder = new EnvelopeDecoderImpl();

		final List<M3daEnvelope> list = new ArrayList<M3daEnvelope>();
		final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void decoded(final M3daEnvelope pdu) {
				LOG.debug("PDU : {}", pdu);
				list.add(pdu);
			}
		};
		// run
		// we decode received chunk of a full M3da envelope
		decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(chunk1.toCharArray())), output);
		decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(chunk2.toCharArray())), output);
		decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(chunk3.toCharArray())), output);
		decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(chunk4.toCharArray())), output);

		assertEquals(1, list.size());

		final M3daEnvelope env = list.get(0);

		final Map<Object, Object> header = Maps.newHashMap();
		header.put("dev", toBB("123456789ABCDEF"));
		header.put("id", new Integer(123456));

		assertEquals(header, env.getHeader());
		assertEquals(Collections.EMPTY_MAP, env.getFooter());

		// decode the payload
		final ByteBuffer payload = ByteBuffer.wrap(env.getPayload());

		final List<Object> bodyList = new ArrayList<Object>();
		final DecoderOutput<M3daPdu> bodyOutput = new DecoderOutput<M3daPdu>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void decoded(final M3daPdu pdu) {
				LOG.debug("Body PDU : {}", pdu);
				bodyList.add(pdu);
			}
		};
		final BysantDecoder bysantDecoder = new BysantDecoderImpl();
		bysantDecoder.decodeAndAccumulate(payload, bodyOutput);
		assertEquals(1, bodyList.size());

		final Map<Object, Object> body = Maps.newHashMap();
		body.put("A.B.C.D.E.F.G.H.I.J.K.L.M.O.P.Q.R.S.T.U.V.W.X.Y.Z.A.B.C.D.E.F.G.H.I.J.K.L.M.O.P.Q.R.S.T.U.V.W.X.Y.Z.ridiculouslylongpath",
				Lists.newArrayList(toBB("string"), 12, -50000, 450000000, Lists.newArrayList(toBB("A"), toBB("B"), 123)));
		final M3daMessage msgOrigin = new M3daMessage("myAsset.somewhere", 12L, body);

		final M3daMessage msgDecoded = (M3daMessage) bodyList.get(0);

		assertEquals(msgOrigin, msgDecoded);
	}

	@Test
	public void exception_on_trailling_null_objects() throws DecoderException, m3da.server.codec.DecoderException {
		final String toDecode = "6084036964123232323030303232323030303232321f610540737973b684ff4e16c30e1374657374206465206465636f6461676583000000";
		final EnvelopeDecoder decoder = new EnvelopeDecoderImpl();
		final List<M3daEnvelope> list = new ArrayList<M3daEnvelope>();
		final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void decoded(final M3daEnvelope pdu) {
				LOG.debug("PDU : {}", pdu);
				list.add(pdu);
			}
		};

		decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(toDecode.toCharArray())), output);

		try {
			decoder.finishDecode();
			fail();
		} catch (final m3da.server.codec.DecoderException ex) {
			LOG.info("happy  exception", ex);
		}
	}

	@Test
	public void exception_on_trailling_incomplete_objects() throws DecoderException, m3da.server.codec.DecoderException {
		final String toDecode = "6084036964123232323030303232323030303232321f610540737973b684ff4e16c30e1374657374206465206465636f64616765833041";
		final EnvelopeDecoder decoder = new EnvelopeDecoderImpl();
		final List<M3daEnvelope> list = new ArrayList<M3daEnvelope>();
		final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void decoded(final M3daEnvelope pdu) {
				LOG.debug("PDU : {}", pdu);
				list.add(pdu);
			}
		};

		decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(toDecode.toCharArray())), output);
		try {
			decoder.finishDecode();
			fail();
		} catch (final m3da.server.codec.DecoderException ex) {
			LOG.info("happy  exception", ex);
		}
	}

	@Test
	public void exception_on_corrupt_stream() throws DecoderException, m3da.server.codec.DecoderException {
		final String toDecode = "607e7e6964123232323030303232323030303232321f610540737973b684ff4e16c30e1374657374206465206465636f6461676583";
		final EnvelopeDecoder decoder = new EnvelopeDecoderImpl();
		final List<M3daEnvelope> list = new ArrayList<M3daEnvelope>();
		final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void decoded(final M3daEnvelope pdu) {
				LOG.debug("PDU : {}", pdu);
				list.add(pdu);
			}
		};

		try {
			decoder.decodeAndAccumulate(ByteBuffer.wrap(Hex.decodeHex(toDecode.toCharArray())), output);
			fail();
		} catch (final m3da.server.codec.DecoderException ex) {
			LOG.info("happy  exception", ex);
		}
	}

	/**
	 * dumb perf test, to be run only for testing perfs
	 */
	@Test
	@Ignore
	public void decode_lot_of_sessions() throws DecoderException, m3da.server.codec.DecoderException {
		final String toDecode = "6085036964f1d9ff0464657612313233343536373839414243444546317761126d7941737365742e736f6d65776865726547843148412e422e432e442e452e462e472e482e492e4a2e4b2e4c2e4d2e4f2e502e512e522e532e542e552e562e572e582e592e5a2e412e422e432e442e452e462e472e482e492e4a2e4b2e4c2e4d2e4f2e502e512e522e532e542e552e562e572e582e592e5a2e7269646963756c6f75736c796c6f6e67706174682f09737472696e67abf4bb30fc1ad274802d04410442e03a83";

		// some simple message with one data
		// String toDecode =
		// "6084036964123232323030303232323030303232321f610540737973b684ff4e16c30e1374657374206465206465636f6461676583";
		final ByteBuffer data = ByteBuffer.wrap(Hex.decodeHex(toDecode.toCharArray()));

		final DecoderOutput<M3daPdu> bodyOutput = new DecoderOutput<M3daPdu>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void decoded(final M3daPdu pdu) {
				// NOP
			}
		};

		final DecoderOutput<M3daEnvelope> output = new DecoderOutput<M3daEnvelope>() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void decoded(final M3daEnvelope pdu) {
				// decode envelope
				final BysantDecoder bysantDecoder = new BysantDecoderImpl();
				try {
					bysantDecoder.decodeAndAccumulate(ByteBuffer.wrap(pdu.getPayload()), bodyOutput);
					bysantDecoder.finishDecode();
				} catch (final m3da.server.codec.DecoderException e) {
					LOG.error("ex ", e);
				}
			}
		};

		// warmup
		for (int i = 0; i < 100; i++) {
			final EnvelopeDecoder decoder = new EnvelopeDecoderImpl();
			decoder.decodeAndAccumulate(data, output);
			data.rewind();
		}

		// run for 200k message decode
		final long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			final EnvelopeDecoder decoder = new EnvelopeDecoderImpl();
			decoder.decodeAndAccumulate(data, output);
			decoder.finishDecode();
			data.rewind();
		}

		final long stopTime = System.currentTimeMillis();
		System.err.println("duration : " + (stopTime - startTime));
	}
}
