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

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

import m3da.server.codec.dto.M3daEnvelope;
import m3da.server.codec.dto.M3daMessage;
import m3da.server.codec.impl.EnvelopeEncoderImpl;
import m3da.server.codec.impl.M3daCodecServiceImpl;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Unit test for {@link EnvelopeEncoderImpl}
 */
public class M3daEncoderImplTest {

	private final M3daCodecService service = new M3daCodecServiceImpl();

	@SuppressWarnings("unchecked")
	@Test
	public void encode_a_multi_dimension_awt_message() {

		// prepare

		Map<Object, Object> body = Maps.newHashMap();
		body.put("A.B.C.D.E.F.G.H.I.J.K.L.M.O.P.Q.R.S.T.U.V.W.X.Y.Z.A.B.C.D.E.F.G.H.I.J.K.L.M.O.P.Q.R.S.T.U.V.W.X.Y.Z.ridiculouslylongpath",
				Lists.newArrayList("string", 12, -50000, 450000000, Lists.newArrayList("A", "B", 123)));
		M3daMessage msg = new M3daMessage("myAsset.somewhere", 12L, body);

		BysantEncoder bodyEncoder = service.createBodyEncoder();

		// run
		ByteBuffer bodyBuff = bodyEncoder.encode(msg);

		Map<Object, Object> values = Maps.newHashMap();
		values.put("dev", "123456789ABCDEF");
		values.put("id", new Integer(123456));

		M3daEnvelope envelope = new M3daEnvelope(values, bodyBuff.array(), Collections.emptyMap());

		EnvelopeEncoder envEncoder = service.createEnvelopeEncoder();

		ByteBuffer buffer = envEncoder.encode(envelope);

		// verify
		assertEquals(
				"6085036964f1d9ff0464657612313233343536373839414243444546317761126d7941737365742e736f6d65776865726547843148412e422e432e442e452e462e472e482e492e4a2e4b2e4c2e4d2e4f2e502e512e522e532e542e552e562e572e582e592e5a2e412e422e432e442e452e462e472e482e492e4a2e4b2e4c2e4d2e4f2e502e512e522e532e542e552e562e572e582e592e5a2e7269646963756c6f75736c796c6f6e67706174682f09737472696e67abf4bb30fc1ad274802d04410442e03a83",
				new String(Hex.encodeHex(buffer.array())));
	}

}
