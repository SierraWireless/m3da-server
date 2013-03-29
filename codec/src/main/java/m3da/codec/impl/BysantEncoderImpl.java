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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import m3da.codec.BysantEncoder;
import m3da.codec.M3daCodecServiceRuntimeException;
import m3da.codec.dto.M3daDeltasVector;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.dto.M3daMessage;
import m3da.codec.dto.M3daPdu;
import m3da.codec.dto.M3daQuasiPeriodicVector;
import m3da.codec.dto.M3daResponse;
import m3da.codec.impl.encoding.BooleanEncoding;
import m3da.codec.impl.encoding.Encoding;
import m3da.codec.impl.encoding.ListEncoding;
import m3da.codec.impl.encoding.M3daEncoding;
import m3da.codec.impl.encoding.MapEncoding;
import m3da.codec.impl.encoding.NumberEncoding;
import m3da.codec.impl.encoding.StringEncoding;
import m3da.codec.impl.encoding.UintEncoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Low level Bysant encoder, state-less and thread safe. Create a {@link ByteBuffer} containing all the given objects encoded following the bysant
 * spec
 */
public class BysantEncoderImpl implements BysantEncoder {

	private static final Logger LOG = LoggerFactory.getLogger(BysantEncoderImpl.class);

	public static final short NULL = (short) 0x00;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer encode(final M3daPdu... toEncode) {
		return encode(BysantContext.GLOBAL, toEncode);
	}

	/**
	 * encode some object in bysant, in a given context
	 * 
	 * @param ctx
	 * @param toEncode
	 * @return
	 */
	public ByteBuffer encode(final BysantContext ctx, final Object[] toEncode) {

		LOG.debug("encode({},objects:{})", ctx, toEncode);

		int bufferSize = 0;
		for (final Object obj : toEncode) {
			bufferSize += guessSizeObject(ctx, obj);
		}

		final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		for (final Object obj : toEncode) {
			encodeObj(ctx, obj, buffer);
		}
		buffer.flip();
		return buffer;
	}

	/**
	 * Guess the encoding size for any Object in a given context
	 */
	private int guessSizeObject(final BysantContext ctx, final Object data) {
		if (data == null) {
			return 1;
		} else if (data instanceof Map) {
			return guessSizeMap(ctx, (Map<?, ?>) data);
		} else if (data instanceof List) {
			return guessSizeList(ctx, (List<?>) data);
		} else if (data instanceof String) {
			return guessSizeString(ctx, (String) data);
		} else if (data instanceof Boolean) {
			return guessSizeBoolean(ctx, (Boolean) data);
		} else if (data instanceof ByteBuffer[]) {
			return guessSizeBinary(ctx, (ByteBuffer[]) data);
		} else if (data instanceof byte[]) {
			return guessSizeBinary(ctx, (byte[]) data);
		} else if (data instanceof ByteBuffer) {
			return guessSizeBinary(ctx, (ByteBuffer) data);
		} else if (data instanceof Number) {
			return guessSizeNumber(ctx, (Number) data);
		} else if (data instanceof M3daEnvelope) {
			return guessSizeEnvelope(ctx, (M3daEnvelope) data);
		} else if (data instanceof M3daMessage) {
			return guessSizeMessage(ctx, (M3daMessage) data);
		} else if (data instanceof M3daResponse) {
			return guessSizeResponse(ctx, (M3daResponse) data);
		} else if (data instanceof M3daDeltasVector) {
			return guessSizeDeltasVector(ctx, (M3daDeltasVector) data);
		} else if (data instanceof M3daQuasiPeriodicVector) {
			return guessSizeQuasiPeriodicVector(ctx, (M3daQuasiPeriodicVector) data);
		} else {
			throw new IllegalStateException("not implemented " + data.getClass().getCanonicalName());
		}
	}

	private int guessSizeEnvelope(final BysantContext ctx, final M3daEnvelope envelope) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Envelope in the context " + ctx);
		}
		int size = 1; // ope code
		size += guessSizeMap(BysantContext.LIST_AND_MAPS, envelope.getHeader());
		size += guessSizeBinary(BysantContext.UINTS_AND_STRS, new ByteBuffer[] { ByteBuffer.wrap(envelope.getPayload()) });
		size += guessSizeMap(BysantContext.LIST_AND_MAPS, envelope.getFooter());
		return size;
	}

	private int guessSizeMessage(final BysantContext ctx, final M3daMessage message) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Message in the context " + ctx);
		}
		int size = 1; // ope code
		size += guessSizeString(BysantContext.UINTS_AND_STRS, message.getPath());
		size += guessSizeNumber(BysantContext.UINTS_AND_STRS, message.getTicketId());
		size += guessSizeMap(BysantContext.LIST_AND_MAPS, message.getBody());
		return size;
	}

	private int guessSizeResponse(final BysantContext ctx, final M3daResponse response) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Response in the context " + ctx);
		}
		int size = 1; // ope code
		size += guessSizeNumber(BysantContext.UINTS_AND_STRS, response.getTicketId());
		size += guessSizeNumber(BysantContext.NUMBERS, response.getStatus());
		size += guessSizeString(BysantContext.UINTS_AND_STRS, response.getMessage());
		return size;
	}

	private int guessSizeQuasiPeriodicVector(final BysantContext ctx, final M3daQuasiPeriodicVector vector) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Response in the context " + ctx);
		}
		int size = 1; // ope code
		size += guessSizeNumber(BysantContext.NUMBERS, vector.getPeriod());
		size += guessSizeNumber(BysantContext.NUMBERS, vector.getStart());
		size += guessSizeList(BysantContext.LIST_AND_MAPS, vector.getShifts());
		return size;
	}

	private int guessSizeDeltasVector(final BysantContext ctx, final M3daDeltasVector vector) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Response in the context " + ctx);
		}
		int size = 1; // ope code
		size += guessSizeNumber(BysantContext.NUMBERS, vector.getFactor());
		size += guessSizeNumber(BysantContext.NUMBERS, vector.getStart());
		size += guessSizeList(BysantContext.LIST_AND_MAPS, vector.getDeltas());

		return size;
	}

	/**
	 * Guess the encoding size for a List in a given context
	 */
	private int guessSizeList(final BysantContext ctx, final List<?> list) {
		LOG.trace("guessSizeInBytes({},list:{})", ctx, list);

		final Encoding enc = ctx.getEncoding();
		if (!(enc instanceof ListEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a list in the context " + ctx);
		}
		final ListEncoding encodingCtx = (ListEncoding) enc;

		if (list == null || list.size() == 0) {
			return 1;
		} else {
			int size = 1; // OPCODE
			if (list.size() > encodingCtx.tinyListLimit()) {
				// for big list we need to encode the size
				size += guessSizeNumber(BysantContext.UINTS_AND_STRS, list.size() - encodingCtx.tinyListLimit() - 1);
			}

			for (final Object obj : list) {
				size += guessSizeObject(BysantContext.GLOBAL, obj);
			}
			return size;
		}
	}

	/**
	 * Guess the encoding size for a Map in a given context
	 */
	private int guessSizeMap(final BysantContext ctx, final Map<?, ?> map) {
		LOG.trace("guessSizeInBytes({},map:{})", ctx, map);
		final Encoding enc = ctx.getEncoding();
		if (!(enc instanceof MapEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a map in the context " + ctx);
		}

		final MapEncoding encodingCtx = (MapEncoding) enc;

		if (map == null || map.size() == 0) {
			return 1;
		} else {
			int size = 1; // OPCODE
			if (map.size() > encodingCtx.tinyMapLimit()) {
				// for big list we need to encode the size
				size += guessSizeNumber(BysantContext.UINTS_AND_STRS, map.size() - encodingCtx.tinyMapLimit() - 1);
			}

			// encode the map body
			for (final Map.Entry<?, ?> entry : map.entrySet()) {

				// key size
				size += guessSizeObject(BysantContext.UINTS_AND_STRS, entry.getKey());

				// value size
				size += guessSizeObject(BysantContext.GLOBAL, entry.getValue());
			}

			return size;
		}
	}

	/**
	 * Guess the encoding size for a Integer in a given context
	 */
	private int guessSizeNumber(final BysantContext ctx, final Number value) {
		LOG.trace("guessSizeLong({},integer:{})", ctx, value);

		final Encoding enc = ctx.getEncoding();

		if (enc instanceof UintEncoding) {
			// unsigned int encoding is different and have specific limitation : only positive numbers
			final UintEncoding encodingCtx = (UintEncoding) enc;

			if (value instanceof Float || value instanceof Double) {
				throw new IllegalArgumentException("cannot be float number : " + value + " in UINTS_AND_STRS context");
			}
			if (value == null) {
				return 1;
			} else {
				if (value.longValue() < 0) {
					throw new IllegalArgumentException("cannot be negative : " + value + " in UINTS_AND_STRS context");
				}

				if (value.longValue() <= encodingCtx.tinyMax()) {
					return 1;
				} else if (value.longValue() <= encodingCtx.smallMax()) {
					return 2;
				} else if (value.longValue() <= encodingCtx.mediumMax()) {
					return 3;
				} else if (value.longValue() <= encodingCtx.largeMax()) {
					return 4;
				} else {
					return 5;
				}
			}
		} else if (enc instanceof NumberEncoding) {
			final NumberEncoding encodingCtx = (NumberEncoding) enc;
			if (value == null) {
				return 1;
			} else {
				if (value instanceof Float) {
					return 5;
				} else if (value instanceof Double) {
					return 9;
				} else {
					final long longValue = value.longValue();
					if (Integer.MIN_VALUE <= value.longValue() && value.longValue() <= Integer.MAX_VALUE) {
						if (encodingCtx.tinyMin() <= longValue && longValue <= encodingCtx.tinyMax()) {
							return 1;
						} else if (encodingCtx.smallMin() <= longValue && longValue <= encodingCtx.smallMax()) {
							return 2;
						} else if (encodingCtx.mediumMin() <= longValue && longValue <= encodingCtx.mediumMax()) {
							return 3;
						} else if (encodingCtx.largeMin() <= longValue && longValue <= encodingCtx.largeMax()) {
							return 4;
						} else {
							return 5;
						}
					} else {
						// 64 bit integer
						return 9;
					}
				}
			}
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a number in the context " + ctx);
		}
	}

	/**
	 * Guess the encoding size for a String in a given context
	 */
	private int guessSizeString(final BysantContext ctx, final String value) {
		LOG.trace("guessSizeString({},string:{})", ctx, value);

		StringEncoding encodingCtx;

		final Encoding enc = ctx.getEncoding();
		if (enc instanceof StringEncoding) {
			encodingCtx = (StringEncoding) enc;
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a string in the context " + ctx);
		}

		if (value == null) {
			return 1;
		}

		int length;

		try {
			length = value.getBytes("UTF8").length;
		} catch (final UnsupportedEncodingException e) {
			throw new M3daCodecServiceRuntimeException("no UTF8 charset", e);
		}

		if (length <= encodingCtx.tinyStrLimit()) {
			return 1 + length;
		}
		if (length <= encodingCtx.smallStrLimit()) {
			return 1 + 1 + length;
		}
		if (length <= encodingCtx.largeStrLimit()) {
			return 1 + 2 + length;
		}

		final int size = 1 + (length / 65535) * 2 + 2 + length;

		return length % 65535 == 0 ? size : size + 2;
	}

	/**
	 * Guess the encoding size for a Boolean in a given context
	 */
	private int guessSizeBoolean(final BysantContext ctx, final Boolean value) {
		LOG.trace("guessSizeBoolean({},boolean:{})", ctx, value);

		final Encoding enc = ctx.getEncoding();

		if (enc instanceof BooleanEncoding) {
			return 1;
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a boolean in the context " + ctx);
		}

	}

	private int guessSizeBinary(final BysantContext ctx, final byte[] value) {
		return guessSizeBinary(ctx, new ByteBuffer[] { ByteBuffer.wrap(value) });
	}

	private int guessSizeBinary(final BysantContext ctx, final ByteBuffer value) {
		return guessSizeBinary(ctx, new ByteBuffer[] { value });
	}

	/**
	 * Guess the encoding size for a Binary in a given context
	 */
	private int guessSizeBinary(final BysantContext ctx, final ByteBuffer[] value) {
		LOG.trace("guessSizeBinary({},string:{})", ctx, value);
		if (value == null) {
			return 1;
		}

		StringEncoding encodingCtx;

		int byteSize = 0;

		for (final ByteBuffer buff : value) {
			byteSize += buff.remaining();
		}

		final Encoding enc = ctx.getEncoding();
		if (enc instanceof StringEncoding) {
			encodingCtx = (StringEncoding) enc;
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a binary/string in the context " + ctx);
		}

		if (byteSize <= encodingCtx.tinyStrLimit()) {
			return 1 + byteSize;
		}
		if (byteSize <= encodingCtx.smallStrLimit()) {
			return 2 + byteSize;
		}
		if (byteSize <= encodingCtx.largeStrLimit()) {
			return 3 + byteSize;
		} else {
			// TODO : chunked encoding for too large strings
			throw new IllegalStateException("not implemented chunked-strings");
		}
	}

	/** Encode random object using a given context. The bytes are written in the buffer. */
	private void encodeObj(final BysantContext ctx, final Object data, final ByteBuffer buffer) {
		if (data == null) {
			buffer.put((byte) NULL);
		} else if (data instanceof Map) {
			encodeMap(ctx, (Map<?, ?>) data, buffer);
		} else if (data instanceof List) {
			encodeList(ctx, (List<?>) data, buffer);
		} else if (data instanceof String) {
			encodeString(ctx, (String) data, buffer);
		} else if (data instanceof Boolean) {
			encodeBoolean(ctx, (Boolean) data, buffer);
		} else if (data instanceof ByteBuffer[]) {
			encodeBinary(ctx, (ByteBuffer[]) data, buffer);
		} else if (data instanceof ByteBuffer) {
			encodeBinary(ctx, (ByteBuffer) data, buffer);
		} else if (data instanceof byte[]) {
			encodeBinary(ctx, (byte[]) data, buffer);
		} else if (data instanceof Number) {
			encodeNumber(ctx, (Number) data, buffer);
		} else if (data instanceof M3daMessage) {
			encodeMessage(ctx, (M3daMessage) data, buffer);
		} else if (data instanceof M3daResponse) {
			encodeResponse(ctx, (M3daResponse) data, buffer);
		} else if (data instanceof M3daDeltasVector) {
			encodeDeltasVector(ctx, (M3daDeltasVector) data, buffer);
		} else if (data instanceof M3daQuasiPeriodicVector) {
			encodeQuasiPeriodicVector(ctx, (M3daQuasiPeriodicVector) data, buffer);
		} else if (data instanceof M3daEnvelope) {
			encodeAwtDa3Envelope(ctx, (M3daEnvelope) data, buffer);
		} else {
			throw new IllegalStateException("not implemented " + data.getClass().getCanonicalName());
		}

	}

	private void encodeAwtDa3Envelope(final BysantContext ctx, final M3daEnvelope envelope, final ByteBuffer buffer) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Envelope in the context " + ctx);
		}
		buffer.put((byte) ((M3daEncoding) ctx.getEncoding()).getEnvelopeOpCode());
		encodeMap(BysantContext.LIST_AND_MAPS, envelope.getHeader(), buffer);
		encodeBinary(BysantContext.UINTS_AND_STRS, new ByteBuffer[] { ByteBuffer.wrap(envelope.getPayload()) }, buffer);
		encodeMap(BysantContext.LIST_AND_MAPS, envelope.getFooter(), buffer);
	}

	private void encodeMessage(final BysantContext ctx, final M3daMessage message, final ByteBuffer buffer) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Message in the context " + ctx);
		}
		buffer.put((byte) ((M3daEncoding) ctx.getEncoding()).getMessageOpCode());
		encodeString(BysantContext.UINTS_AND_STRS, message.getPath(), buffer);
		encodeNumber(BysantContext.UINTS_AND_STRS, message.getTicketId(), buffer);
		encodeMap(BysantContext.LIST_AND_MAPS, message.getBody(), buffer);
	}

	private void encodeResponse(final BysantContext ctx, final M3daResponse response, final ByteBuffer buffer) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Response in the context " + ctx);
		}
		buffer.put((byte) ((M3daEncoding) ctx.getEncoding()).getResponseOpCode());
		encodeNumber(BysantContext.UINTS_AND_STRS, response.getTicketId(), buffer);
		encodeNumber(BysantContext.NUMBERS, response.getStatus(), buffer);
		encodeString(BysantContext.UINTS_AND_STRS, response.getMessage(), buffer);
	}

	private void encodeQuasiPeriodicVector(final BysantContext ctx, final M3daQuasiPeriodicVector vector, final ByteBuffer buffer) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Response in the context " + ctx);
		}
		buffer.put((byte) ((M3daEncoding) ctx.getEncoding()).getQuasiPeriodicVectorOpCode());
		encodeNumber(BysantContext.NUMBERS, vector.getPeriod(), buffer);
		encodeNumber(BysantContext.NUMBERS, vector.getStart(), buffer);
		encodeList(BysantContext.LIST_AND_MAPS, vector.getShifts(), buffer);
	}

	private void encodeDeltasVector(final BysantContext ctx, final M3daDeltasVector vector, final ByteBuffer buffer) {
		if (!(ctx.getEncoding() instanceof M3daEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a AwtDa3Response in the context " + ctx);
		}
		buffer.put((byte) ((M3daEncoding) ctx.getEncoding()).getDeltaVectorOpCode());
		encodeNumber(BysantContext.NUMBERS, vector.getFactor(), buffer);
		encodeNumber(BysantContext.NUMBERS, vector.getStart(), buffer);
		encodeList(BysantContext.LIST_AND_MAPS, vector.getDeltas(), buffer);
	}

	/**
	 * Encode a List in a given context. The bytes are written in the buffer.
	 */
	private void encodeList(final BysantContext ctx, final List<?> list, final ByteBuffer buffer) {
		int pos = 0;
		int size = 0;
		if (LOG.isTraceEnabled()) {
			LOG.trace("encode({},list:{})", ctx, list);
			pos = buffer.position();
			size = guessSizeList(ctx, list);
		}
		final Encoding enc = ctx.getEncoding();
		if (!(enc instanceof ListEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a list in the context " + ctx);
		}
		final ListEncoding encodingCtx = (ListEncoding) enc;

		if (list == null) {
			buffer.put((byte) NULL);
		} else if (list.size() == 0) {
			buffer.put((byte) encodingCtx.emptyListOpCode());
		} else {
			if (list.size() <= encodingCtx.tinyListLimit()) {
				final int opCode = encodingCtx.tinyUntypedListOpCode();
				buffer.put((byte) (opCode + list.size() - 1));
			} else {
				buffer.put((byte) encodingCtx.largeUntypedListOpCode());
				// encode the list size
				encodeNumber(BysantContext.UINTS_AND_STRS, (long) (list.size() - encodingCtx.tinyListLimit() - 1), buffer);
			}

			// now we encode all the list element, one after each other
			for (final Object object : list) {
				encodeObj(BysantContext.GLOBAL, object, buffer);
			}
		}
		if (LOG.isTraceEnabled()) {
			final int wrote = buffer.position() - pos;
			if (wrote == size) {
				LOG.trace("wrote {} bytes", wrote);
			} else {
				LOG.error("BUG ! wrote {} bytes, on {} supposed", wrote, size);
			}
		}
	}

	/**
	 * Encode a Map in a given context. The bytes are written in the buffer.
	 */
	private void encodeMap(final BysantContext ctx, final Map<?, ?> map, final ByteBuffer buffer) {
		int pos = 0;
		int byteSize = 0;
		if (LOG.isTraceEnabled()) {
			LOG.trace("encode({},map:{})", ctx, map);
			pos = buffer.position();
			byteSize = guessSizeMap(ctx, map);
		}

		final Encoding enc = ctx.getEncoding();
		if (!(enc instanceof MapEncoding)) {
			throw new M3daCodecServiceRuntimeException("cannot encode a map in the context " + ctx);
		}

		final MapEncoding encodingCtx = (MapEncoding) enc;

		if (map == null) {
			buffer.put((byte) NULL);
		} else if (map.size() == 0) {
			buffer.put((byte) encodingCtx.emptyMapOpCode());
		} else {
			final long size = map.size();
			if (size < encodingCtx.tinyMapLimit()) {
				final int opCode = encodingCtx.tinyUntypedMapOpCode();

				// encode the map size with the opcode
				buffer.put((byte) (opCode + map.size() - 1));
			} else {
				buffer.put((byte) encodingCtx.largeUntypedMapOpCode());
				// encode the map size
				encodeNumber(BysantContext.UINTS_AND_STRS, (long) (map.size() - encodingCtx.tinyMapLimit() - 1), buffer);
			}
			// encode the map body
			for (final Map.Entry<?, ?> entry : map.entrySet()) {

				// key
				final Object key = entry.getKey();
				if (key instanceof Integer) {
					encodeObj(BysantContext.UINTS_AND_STRS, ((Integer) key).longValue(), buffer);
				} else if (key instanceof String || key instanceof ByteBuffer[] || key instanceof byte[] || key instanceof ByteBuffer) {
					encodeObj(BysantContext.UINTS_AND_STRS, key, buffer);
				} else {
					throw new IllegalArgumentException("map key should be a unsigned integer or a string");
				}

				// value
				final Object value = entry.getValue();
				encodeObj(BysantContext.GLOBAL, value, buffer);
			}
		}

		if (LOG.isTraceEnabled()) {
			final int wrote = buffer.position() - pos;
			if (wrote == byteSize) {
				LOG.trace("wrote {} bytes", wrote);
			} else {
				LOG.error("BUG ! wrote {} bytes, on {} supposed", wrote, byteSize);
			}
		}
	}

	/**
	 * Encode a Long in a given context. The bytes are written in the buffer.
	 */
	private void encodeNumber(final BysantContext ctx, final Number value, final ByteBuffer buffer) {
		int pos = 0;
		int byteSize = 0;
		if (LOG.isTraceEnabled()) {
			LOG.trace("encodeNumber({},number:{})", ctx, value);
			pos = buffer.position();
			byteSize = guessSizeNumber(ctx, value);
			LOG.trace("byteSize = {}", byteSize);
		}

		final Encoding enc = ctx.getEncoding();

		if (enc instanceof UintEncoding) {
			// unsigned int encoding is different and have specific limitation : only positive numbers
			final UintEncoding encodingCtx = (UintEncoding) enc;

			if (value == null) {
				buffer.put((byte) NULL);
			} else {

				if (value instanceof Float || value instanceof Double) {
					throw new IllegalArgumentException("cannot encode float number in UINTS_AND_STRS context");
				}
				final long longValue = value.longValue();
				if (longValue < 0) {
					throw new IllegalArgumentException("cannot be negative : " + longValue + " in UINTS_AND_STRS context");
				}

				if (longValue <= encodingCtx.tinyMax()) {
					buffer.put((byte) (encodingCtx.tinyOpeCode() + longValue));
				} else if (longValue <= encodingCtx.smallMax()) {
					final int offset = (int) (longValue - (encodingCtx.tinyMax() + 1));
					buffer.put((byte) (encodingCtx.smallOpeCode() + (offset >> 8)));
					buffer.put((byte) (offset & 0xff));
				} else if (longValue <= encodingCtx.mediumMax()) {
					final int offset = (int) (longValue - (encodingCtx.smallMax() + 1));
					buffer.put((byte) (encodingCtx.mediumOpeCode() + (offset >> 16)));
					buffer.putShort((short) (offset & 0xFFFF));
				} else if (longValue <= encodingCtx.largeMax()) {
					final int offset = (int) (longValue - (encodingCtx.mediumMax() + 1));
					buffer.put((byte) (encodingCtx.largeOpeCode() + (offset >> 24)));
					buffer.put((byte) ((offset & 0xFF0000) >> 16));
					buffer.put((byte) ((offset & 0x00FF00) >> 8));
					buffer.put((byte) (offset & 0x0000FF));
				} else {
					buffer.put((byte) encodingCtx.uint32OpeCode());
					// mask away the sign (just in case)
					buffer.putInt((int) (longValue & 0xFFFFFFFF));
				}
			}
		} else if (enc instanceof NumberEncoding) {

			final NumberEncoding encodingCtx = (NumberEncoding) enc;

			if (value == null) {
				buffer.put((byte) NULL);
			} else {
				if (value instanceof Float) {
					buffer.put((byte) encodingCtx.float32opCode());
					buffer.putFloat(value.floatValue());
				} else if (value instanceof Double) {
					buffer.put((byte) encodingCtx.float64opCode());
					buffer.putDouble(value.doubleValue());
				} else {
					if (Integer.MIN_VALUE <= value.longValue() && value.longValue() <= Integer.MAX_VALUE) {
						final int intValue = value.intValue();
						if (intValue >= encodingCtx.tinyMin() && intValue <= encodingCtx.tinyMax()) {
							buffer.put((byte) (encodingCtx.tinyOpCode() + intValue - encodingCtx.tinyMin()));
						} else if (intValue >= encodingCtx.smallMin() && intValue <= encodingCtx.smallMax()) {
							final int offset = computeOffset(intValue, encodingCtx.tinyMin(), encodingCtx.tinyMax());
							final int opbase = intValue < 0 ? encodingCtx.smallNegativeOpCode() : encodingCtx.smallPositiveOpCode();
							buffer.put((byte) (opbase + (offset >> 8)));
							buffer.put((byte) (offset & 0xFF));
						} else if (intValue >= encodingCtx.mediumMin() && intValue <= encodingCtx.mediumMax()) {
							final int offset = computeOffset(intValue, encodingCtx.smallMin(), encodingCtx.smallMax());
							final int opbase = intValue < 0 ? encodingCtx.mediumNegativeOpCode() : encodingCtx.mediumPositiveOpCode();

							buffer.put((byte) (opbase + (offset >> 16)));
							buffer.put((byte) ((offset >> 8) & 0xFF));
							buffer.put((byte) (offset & 0xFF));

						} else if (intValue >= encodingCtx.largeMin() && intValue <= encodingCtx.largeMax()) {
							final int offset = computeOffset(intValue, encodingCtx.mediumMin(), encodingCtx.mediumMax());
							final int opbase = intValue < 0 ? encodingCtx.largeNegativeOpCode() : encodingCtx.largePositiveOpCode();

							buffer.put((byte) (opbase + (offset >> 24)));
							buffer.put((byte) ((offset >> 16) & 0xFF));
							buffer.put((byte) ((offset >> 8) & 0xFF));
							buffer.put((byte) (offset & 0xFF));
						} else {
							// 32bit integer
							buffer.put((byte) encodingCtx.int32OpCode());
							buffer.putInt(intValue);
						}
					} else {
						// 64 bit integer
						buffer.put((byte) encodingCtx.int64OpCode());
						buffer.putLong(value.longValue());
					}
				}
			}
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a number in the context " + ctx);
		}

		if (LOG.isTraceEnabled()) {
			final int wrote = buffer.position() - pos;
			if (wrote == byteSize) {
				LOG.trace("wrote {} bytes", wrote);
			} else {
				LOG.error("BUG ! wrote {} bytes, on {} supposed", wrote, byteSize);
			}
		}
	}

	// compute the encoding offset for signed number encoding
	// see the bysant spec formulas
	private static int computeOffset(final int value, final int ifneg, final int ifpos) {
		return value < 0 ? ((-value) - (-ifneg + 1)) : (value - (ifpos + 1));
	}

	/**
	 * Encode a String in a given context. The bytes are written in the buffer.
	 */
	private void encodeString(final BysantContext ctx, final String value, final ByteBuffer buffer) {
		int pos = 0;
		int byteSize = 0;
		if (LOG.isTraceEnabled()) {
			LOG.trace("encode({},string:{})", ctx, value);
			pos = buffer.position();
			byteSize = guessSizeString(ctx, value);
			LOG.trace("byteSize : {}", byteSize);
		}

		StringEncoding encodingCtx;

		final Encoding enc = ctx.getEncoding();
		if (enc instanceof StringEncoding) {
			encodingCtx = (StringEncoding) enc;
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a string in the context " + ctx);
		}

		if (value == null) {
			buffer.put((byte) NULL);
		} else {
			byte[] rawUtf8;
			int length;
			try {
				rawUtf8 = value.getBytes("UTF8");
				length = rawUtf8.length;
			} catch (final UnsupportedEncodingException e) {
				throw new M3daCodecServiceRuntimeException("no UTF8 charset", e);
			}

			if (length <= encodingCtx.tinyStrLimit()) {
				// 1 BYTES length
				buffer.put((byte) (encodingCtx.tinyStrOpeCode() + length));
				// encode the string
				buffer.put(rawUtf8);
			} else if (length <= encodingCtx.smallStrLimit()) {
				// 2 BYTES length
				final int len = length - encodingCtx.tinyStrLimit() - 1;
				buffer.putShort((short) ((len & 0xFF00) + (encodingCtx.smallStrOpeCode() << 8) + (len & 0x00FF)));
				// encode the string
				buffer.put(rawUtf8);
			} else if (length <= encodingCtx.largeStrLimit()) {
				// 3 BYTES length
				final int len = length - encodingCtx.smallStrLimit() - 1;
				buffer.put((byte) encodingCtx.largeStrOpeCode());
				buffer.put((byte) ((len & 0x00FF00) >> 8));
				buffer.put((byte) (len & 0x0000FF));
				// encode the string
				buffer.put(rawUtf8);
			} else {
				buffer.put((byte) encodingCtx.chunkedStrOpeCode());

				int wrote = 0;
				for (int i = 0; i < rawUtf8.length / 65535; i++) {
					LOG.trace("writing chunk number {}", i);
					buffer.putShort((short) 65535);
					buffer.put(rawUtf8, wrote, 65535);
					wrote += 65535;
				}
				final int remaining = rawUtf8.length - wrote;
				LOG.trace("remaining : {} vs {}", remaining, buffer.remaining());
				if (remaining > 0) {
					buffer.putShort((short) remaining);
					buffer.put(rawUtf8, wrote, remaining);
				}
				buffer.putShort((short) 0x00);
			}
		}

		if (LOG.isTraceEnabled()) {
			final int wrote = buffer.position() - pos;
			if (wrote == byteSize) {
				LOG.trace("wrote {} bytes", wrote);
			} else {
				LOG.error("BUG ! wrote {} bytes, on {} supposed", wrote, byteSize);
			}
		}
	}

	/**
	 * Encode a Boolean in a given context. The bytes are written in the buffer.
	 */
	private void encodeBoolean(final BysantContext ctx, final Boolean value, final ByteBuffer buffer) {
		int pos = 0;
		int byteSize = 0;
		if (LOG.isTraceEnabled()) {
			LOG.trace("encode({},boolean:{})", ctx, value);
			pos = buffer.position();
			byteSize = guessSizeBoolean(ctx, value);
			LOG.trace("byteSize : {}", byteSize);
		}

		BooleanEncoding encodingCtx;

		final Encoding enc = ctx.getEncoding();
		if (enc instanceof BooleanEncoding) {
			encodingCtx = (BooleanEncoding) enc;
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a boolean in the context " + ctx);
		}

		if (value == null) {
			buffer.put((byte) NULL);
		} else if (value) {
			buffer.put((byte) encodingCtx.getTrueOpCode());
		} else {
			buffer.put((byte) encodingCtx.getFalseOpCode());
		}

		if (LOG.isTraceEnabled()) {
			final int wrote = buffer.position() - pos;
			if (wrote == byteSize) {
				LOG.trace("wrote {} bytes", wrote);
			} else {
				LOG.error("BUG ! wrote {} bytes, on {} supposed", wrote, byteSize);
			}
		}
	}

	private void encodeBinary(final BysantContext ctx, final byte[] inputBuffer, final ByteBuffer buffer) {
		encodeBinary(ctx, new ByteBuffer[] { ByteBuffer.wrap(inputBuffer) }, buffer);
	}

	private void encodeBinary(final BysantContext ctx, final ByteBuffer inputBuffer, final ByteBuffer buffer) {
		encodeBinary(ctx, new ByteBuffer[] { inputBuffer }, buffer);
	}

	/**
	 * Encode a String in a given context. The bytes are written in the buffer.
	 */
	private void encodeBinary(final BysantContext ctx, final ByteBuffer[] inputBuffers, final ByteBuffer buffer) {
		int pos = 0;
		int byteSize = 0;
		if (LOG.isTraceEnabled()) {
			LOG.trace("encode({},binary:{})", ctx, inputBuffers);
			pos = buffer.position();
			byteSize = guessSizeBinary(ctx, inputBuffers);
		}

		StringEncoding encodingCtx;

		final Encoding enc = ctx.getEncoding();
		if (enc instanceof StringEncoding) {
			encodingCtx = (StringEncoding) enc;
		} else {
			throw new M3daCodecServiceRuntimeException("cannot encode a binary/string in the context " + ctx);
		}

		if (inputBuffers == null) {
			buffer.put((byte) NULL);
		} else {

			int length = 0;
			for (final ByteBuffer inBuff : inputBuffers) {
				length += inBuff.remaining();
			}

			if (length <= encodingCtx.tinyStrLimit()) {
				// 1 BYTES length
				buffer.put((byte) (encodingCtx.tinyStrOpeCode() + length));
			} else if (length <= encodingCtx.smallStrLimit()) {
				// 2 BYTES length
				final int len = length - encodingCtx.tinyStrLimit() - 1;
				buffer.putShort((short) ((len & 0xFF00) + (encodingCtx.smallStrOpeCode() << 8) + (len & 0x00FF)));
			} else if (length <= encodingCtx.largeStrLimit()) {
				// 3 BYTES length
				final int len = length - encodingCtx.smallStrLimit() - 1;
				buffer.put((byte) encodingCtx.largeStrOpeCode());
				buffer.put((byte) ((len & 0xFF0000) >> 16));
				buffer.put((byte) ((len & 0x00FF00) >> 8));
				buffer.put((byte) (len & 0x0000FF));
			} else {
				throw new IllegalStateException("not implemented chunked-strings");
			}
			// encode the string
			for (final ByteBuffer inBuff : inputBuffers) {
				buffer.put(inBuff);
			}
		}

		if (LOG.isTraceEnabled()) {
			final int wrote = buffer.position() - pos;
			if (wrote == byteSize) {
				LOG.trace("wrote {} bytes", wrote);
			} else {
				LOG.error("BUG ! wrote {} bytes, on {} supposed", wrote, byteSize);
			}
		}
	}
}
