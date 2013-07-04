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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.codec.BysantDecoder;
import m3da.codec.DecoderException;
import m3da.codec.DecoderOutput;
import m3da.codec.Hex;
import m3da.codec.dto.M3daDeltasVector;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.dto.M3daMessage;
import m3da.codec.dto.M3daPdu;
import m3da.codec.dto.M3daQuasiPeriodicVector;
import m3da.codec.dto.M3daResponse;
import m3da.codec.impl.encoding.BooleanEncoding;
import m3da.codec.impl.encoding.Encoding;
import m3da.codec.impl.encoding.GlobalCtxEncoding;
import m3da.codec.impl.encoding.ListEncoding;
import m3da.codec.impl.encoding.M3daEncoding;
import m3da.codec.impl.encoding.MapEncoding;
import m3da.codec.impl.encoding.NumberEncoding;
import m3da.codec.impl.encoding.NumbersCtxEncoding;
import m3da.codec.impl.encoding.StringEncoding;
import m3da.codec.impl.encoding.UintEncoding;
import m3da.codec.impl.encoding.UintStrCtxEncoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Bysant byte stream decoder. <br>
 * WARNING : NOT THREAD SAFE.<br>
 * Will decode a stream of byte and output decoded object to the given callback. Remaining undecoded bytes will be
 * accumulated. Call {@link #finishDecode()} once the
 */
public class BysantDecoderImpl implements BysantDecoder {

    private static final long FOUR_BYTES_MASK = 0xFFFFFFFFL;

    private static final int TWO_BYTES_MASK = 0xFFFF;

    private static final int ONE_BYTE_MASK = 0xFF;

    private static final Logger LOG = LoggerFactory.getLogger(BysantDecoderImpl.class);

    /** the null value opecode */
    public static final short NULL = (short) 0x00;

    /** buffer for accumulating undecoded remaining bytes */
    private ByteBuffer remainingBuffer = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void decodeAndAccumulate(final ByteBuffer buffer, final DecoderOutput<M3daPdu> output)
            throws DecoderException {
        decodeAndAccumulate(buffer, output, BysantContext.GLOBAL);
    }

    /**
     * Decode some buffer and output the decoded object in a {@link DecoderOutput}. If some bytes are remaining
     * undecoded, we accumulate them and decode them during the next call to decode.
     * 
     * @param buffer the input buffer to consume for decoding
     * @param output the output callback , called each time an object is successfully decoded
     * @param context the context to be used for starting decoding this stream
     * @throws DecoderException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void decodeAndAccumulate(final ByteBuffer buffer, final DecoderOutput output, final BysantContext context)
            throws DecoderException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("decodeAndAccumulate (context = {}, buffer = {})", context, dumpBuffer(buffer));
        }
        // we need a least one byte
        if (buffer.remaining() <= 0) {
            throw new IllegalArgumentException("buffer need at least one byte remaining");
        }

        // decode in a loop until we reach a buffer exception

        if (remainingBuffer == null) {
            remainingBuffer = ByteBuffer.allocate(buffer.capacity());
            remainingBuffer.put(buffer).flip();
        } else {
            // concatenate the remaining part of the buffers in a new one
            if (LOG.isTraceEnabled()) {
                LOG.trace("some bytes remaining : {}", dumpBuffer(remainingBuffer));
            }
            final int size = remainingBuffer.remaining() + buffer.remaining();
            final ByteBuffer newRemaining = ByteBuffer.allocate(size);
            newRemaining.put(remainingBuffer).put(buffer).flip();
            remainingBuffer = newRemaining;
        }
        LOG.trace("buffer position : {}, remaining {}", remainingBuffer.position(), remainingBuffer.remaining());

        if (LOG.isTraceEnabled()) {
            LOG.trace("buffer concatenated with remaining : {}", dumpBuffer(remainingBuffer));
        }

        // we decode until all bytes are consumed (or if we reach an underflow)
        do {
            final int position = remainingBuffer.position();
            if (LOG.isTraceEnabled()) {
                LOG.trace("save current position : {} in buffer {}", position, dumpBuffer(remainingBuffer));
            }

            try {
                final Object pdu = decodeOne(context.getEncoding(), remainingBuffer);
                output.decoded(pdu);
            } catch (final BufferUnderflowException ex) {
                LOG.trace("buffer underflow ! we need more bytes !");
                // we need to accumulate more bytes !
                // so we rollback to the previous buffer position
                remainingBuffer.position(position);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("rewind buffer {}", dumpBuffer(remainingBuffer));
                    LOG.trace("we keep {} bytes", remainingBuffer.remaining());
                }
                // and we escape
                break;
            }

        } while (remainingBuffer.hasRemaining());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishDecode() throws DecoderException {
        if (remainingBuffer.hasRemaining()) {
            throw new DecoderException("trailling bytes in the bysant accumulator : uncomplete transmition ?");
        }
    }

    /**
     * Decode one object from the buffer
     * 
     * @return the decoded object
     */
    private Object decodeOne(final Encoding encoding, final ByteBuffer buffer) throws DecoderException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("decodeOne( encoding = {}, buffer = {}", encoding, dumpBuffer(buffer));
        }
        if (buffer.remaining() == 0) {
            throw new BufferUnderflowException();
        }
        final int opcode = buffer.get(buffer.position()) & ONE_BYTE_MASK;
        LOG.trace("OPCODE : 0x{}", Integer.toHexString(opcode));

        if (opcode == NULL) {
            // consume the OPCODE
            buffer.get();
            return null;
        }

        if (encoding instanceof StringEncoding) {
            final StringEncoding strEnc = (StringEncoding) encoding;
            // string opcode ?
            if (strEnc.tinyStrOpeCode() <= opcode && opcode <= strEnc.chunkedStrOpeCode()) {
                // decode a string !
                return decodeStringAsBytes(strEnc, buffer);
            }
        }
        if (encoding instanceof NumberEncoding) {
            final NumberEncoding numEnc = (NumberEncoding) encoding;
            // number op code ?
            if ((numEnc instanceof GlobalCtxEncoding) && numEnc.tinyOpCode() <= opcode
                    && opcode <= numEnc.float64opCode()) {
                // decode a number
                return decodeNumber(numEnc, buffer);
            } else if ((numEnc instanceof NumbersCtxEncoding)) {
                // number contain op code only for numbers
                // decode a number
                return decodeNumber(numEnc, buffer);
            }
        }
        if (encoding instanceof UintEncoding) {
            final UintEncoding uintEncoding = (UintEncoding) encoding;
            // unsigned int op code ?
            if (uintEncoding.tinyOpeCode() <= opcode && opcode <= uintEncoding.uint32OpeCode()) {
                return decodeUint(uintEncoding, buffer);
            }
        }
        if (encoding instanceof MapEncoding) {
            final MapEncoding mapEnc = (MapEncoding) encoding;
            // map op code ?
            if (mapEnc.emptyMapOpCode() <= opcode && opcode <= mapEnc.nullTerminatedTypedMapOpCode()) {
                return decodeMap(mapEnc, buffer);
            }
        }
        if (encoding instanceof ListEncoding) {
            final ListEncoding listEnc = (ListEncoding) encoding;
            // list opcode ?
            if (listEnc.emptyListOpCode() <= opcode && opcode <= listEnc.nullTerminatedTypedListOpCode()) {
                return decodeList(listEnc, buffer);
            }
        }
        if (encoding instanceof M3daEncoding) {
            final M3daEncoding awtEnc = (M3daEncoding) encoding;
            // some AWTDA class opcode ?
            if (opcode == awtEnc.getDeltaVectorOpCode() || opcode == awtEnc.getEnvelopeOpCode()
                    || opcode == awtEnc.getMessageOpCode() || opcode == awtEnc.getQuasiPeriodicVectorOpCode()
                    || opcode == awtEnc.getResponseOpCode()) {
                return decodeAwt(awtEnc, buffer);
            }
        }
        if (encoding instanceof BooleanEncoding) {
            final BooleanEncoding boolEnc = (BooleanEncoding) encoding;
            // consume the OPCODE
            buffer.get();
            if (opcode == boolEnc.getFalseOpCode()) {
                return Boolean.FALSE;
            } else if (opcode == boolEnc.getTrueOpCode()) {
                return Boolean.TRUE;
            }
        }
        throw new DecoderException("can't decode, no suitable encoding found for OPECODE : " + opcode);
    }

    /** copy 'size' byte of a buffer into a new one */
    private ByteBuffer deepRangeCopy(final ByteBuffer src, final int size) {
        final ByteBuffer res = ByteBuffer.allocate(size);
        for (int i = 0; i < size; i++) {
            res.put(src.get());
        }
        res.flip();
        return res;
    }

    /** decode a string as an buffer of raw bytes following the bysant spec */
    private ByteBuffer decodeStringAsBytes(final StringEncoding encoding, final ByteBuffer buffer)
            throws DecoderException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("decodeStringAsBytes( encoding = {}, buffer = {}", encoding, dumpBuffer(buffer));
        }
        final int opCode = buffer.get() & ONE_BYTE_MASK;
        LOG.trace("OPCODE : 0x{}", Integer.toHexString(opCode));
        if (opCode == NULL) {
            return null;
        } else if (encoding.tinyStrOpeCode() <= opCode && opCode < encoding.smallStrOpeCode()) {
            LOG.trace("tinyString");
            final int size = opCode - encoding.tinyStrOpeCode();
            return deepRangeCopy(buffer, size);
        } else if (encoding.smallStrOpeCode() <= opCode && opCode < encoding.largeStrOpeCode()) {
            LOG.trace("smallString");
            final int byte0 = buffer.get() & ONE_BYTE_MASK;
            final int size = encoding.tinyStrLimit() + 1 + (opCode - encoding.smallStrOpeCode()) * 256 + byte0;
            return deepRangeCopy(buffer, size);
        } else if (opCode == encoding.largeStrOpeCode()) {
            LOG.trace("largeString");
            final int byte1byte0 = buffer.getShort() & TWO_BYTES_MASK;
            final int size = encoding.smallStrLimit() + 1 + byte1byte0;
            return deepRangeCopy(buffer, size);
        } else if (opCode == encoding.chunkedStrOpeCode()) {
            LOG.trace("chunkedString");
            // chunked string : be careful this one can eat babies and all your memory
            final List<byte[]> chunks = new ArrayList<byte[]>();
            for (;;) {
                final int chunkSize = buffer.getShort() & 0XFFFF;
                LOG.trace("chunk size : {}", chunkSize);
                if (chunkSize == 0) {
                    break;
                }

                final byte[] chunk = new byte[chunkSize];
                buffer.get(chunk);
                chunks.add(chunk);
            }
            int totalSize = 0;
            for (final byte[] chunk : chunks) {
                totalSize += chunk.length;
            }
            LOG.trace("total size : {}", totalSize);
            // concatenate the chunks into a big one
            final ByteBuffer res = ByteBuffer.allocate(totalSize);
            for (final byte[] chunk : chunks) {
                res.put(chunk);
            }
            res.flip();
            return res;
        } else {
            throw new DecoderException("this is not a string OPCODE : " + opCode);
        }
    }

    /** decode a unsigned integer following the bysant spec */
    private Number decodeUint(final UintEncoding encoding, final ByteBuffer buffer) throws DecoderException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("decodeUint( encoding = {}, buffer = {} )", encoding, dumpBuffer(buffer));
        }
        final int opCode = buffer.get() & ONE_BYTE_MASK;
        LOG.trace("OPCODE : 0x{}", Integer.toHexString(opCode));
        if (opCode == NULL) {
            return null;
        } else if (encoding.tinyOpeCode() <= opCode && opCode < encoding.smallOpeCode()) {
            return Integer.valueOf(opCode - encoding.tinyOpeCode());
        } else if (encoding.smallOpeCode() <= opCode && opCode < encoding.mediumOpeCode()) {
            final int byte0 = buffer.get() & ONE_BYTE_MASK;
            final int value = encoding.tinyMax() + 1 + (opCode - encoding.smallOpeCode()) * 256 + byte0;
            return Integer.valueOf(value);
        } else if (encoding.mediumOpeCode() <= opCode && opCode < encoding.largeOpeCode()) {
            final int byte1byte0 = buffer.getShort() & TWO_BYTES_MASK;
            final int value = encoding.smallMax() + 1 + (opCode - encoding.mediumOpeCode()) * 65536 + byte1byte0;
            return Integer.valueOf(value);
        } else if (encoding.largeOpeCode() <= opCode && opCode < encoding.uint32OpeCode()) {
            final int byte2byte1byte0 = (buffer.getShort() & TWO_BYTES_MASK) << 8 | (buffer.get() & ONE_BYTE_MASK);
            final int value = encoding.mediumMax() + 1 + (opCode - encoding.largeOpeCode()) * (1 << 24)
                    + byte2byte1byte0;
            return Integer.valueOf(value);
        } else if (opCode == encoding.uint32OpeCode()) {
            return Long.valueOf(buffer.getInt() & FOUR_BYTES_MASK);
        } else {
            throw new DecoderException("this is not a unsigned int OPCODE : " + opCode);
        }
    }

    /** decode a number (int,long,float,double) following the bysant spec */
    private Number decodeNumber(final NumberEncoding encoding, final ByteBuffer buffer) throws DecoderException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("decodeNumber( encoding = {}, buffer = {}", encoding, dumpBuffer(buffer));
        }
        final int opCode = buffer.get() & ONE_BYTE_MASK;
        LOG.trace("OPCODE : 0x{}", Integer.toHexString(opCode));
        if (opCode == NULL) {
            return null;
        } else if (encoding.tinyOpCode() <= opCode && opCode < encoding.smallPositiveOpCode()) {
            LOG.trace("tiny number");
            return Integer.valueOf(opCode - encoding.tinyOpCode() + encoding.tinyMin());
        } else if (encoding.smallPositiveOpCode() <= opCode && opCode < encoding.smallNegativeOpCode()) {
            // positive small
            LOG.trace("small positive number");
            final int byte0 = buffer.get() & ONE_BYTE_MASK;
            return Integer.valueOf(((opCode - encoding.smallPositiveOpCode()) << 8) + byte0 + encoding.tinyMax() + 1);
        } else if (encoding.smallNegativeOpCode() <= opCode && opCode < encoding.mediumPositiveOpCode()) {
            // negative small
            LOG.trace("small negative number");
            final int byte0 = buffer.get() & ONE_BYTE_MASK;
            return Integer.valueOf(-1 * (((opCode - encoding.smallNegativeOpCode()) << 8) + byte0) + encoding.tinyMin()
                    - 1);
        } else if (encoding.mediumPositiveOpCode() <= opCode && opCode < encoding.mediumNegativeOpCode()) {
            // positive medium
            LOG.trace("medium positive number");
            final int byte1byte0 = buffer.getShort() & TWO_BYTES_MASK;
            return Integer.valueOf(((opCode - encoding.mediumPositiveOpCode()) << 16) + byte1byte0
                    + encoding.smallMax() + 1);
        } else if (encoding.mediumNegativeOpCode() <= opCode && opCode < encoding.largePositiveOpCode()) {
            // negative medium
            LOG.trace("medium negative number");
            final int byte1byte0 = buffer.getShort() & TWO_BYTES_MASK;
            return Integer.valueOf(-1 * (((opCode - encoding.mediumNegativeOpCode()) << 16) + byte1byte0)
                    + encoding.smallMin() - 1);
        } else if (encoding.largePositiveOpCode() <= opCode && opCode < encoding.largeNegativeOpCode()) {
            // positive large
            LOG.trace("large positive number");
            final int byte2byte1byte0 = ((buffer.getShort() & TWO_BYTES_MASK) << 8) + (buffer.get() & ONE_BYTE_MASK);
            return Integer.valueOf(((opCode - encoding.largePositiveOpCode()) << 24) + byte2byte1byte0
                    + encoding.mediumMax() + 1);
        } else if (opCode == encoding.int32OpCode()) {
            LOG.trace("INT32 number");
            return buffer.getInt();
        } else if (opCode == encoding.int64OpCode()) {
            LOG.trace("INT64 number");
            return buffer.getLong();
        } else if (opCode == encoding.float32opCode()) {
            LOG.trace("FLOAT32 number");
            return buffer.getFloat();
        } else if (opCode == encoding.float64opCode()) {
            LOG.trace("FLOAT64 number");
            return buffer.getDouble();
        } else if (((encoding instanceof GlobalCtxEncoding) && encoding.largeNegativeOpCode() <= opCode && opCode < encoding
                .int32OpCode())
                || ((encoding instanceof NumbersCtxEncoding) && encoding.largeNegativeOpCode() <= opCode)) {
            // negative large
            LOG.trace("large negative number");
            final int byte2byte1byte0 = ((buffer.getShort() & TWO_BYTES_MASK) << 8) + (buffer.get() & ONE_BYTE_MASK);
            return Integer.valueOf(-1 * (((opCode - encoding.largeNegativeOpCode()) << 24) + byte2byte1byte0)
                    + encoding.mediumMin() - 1);
        } else {
            throw new DecoderException("this is not a number OPCODE : " + opCode);
        }
    }

    /** decode a list following the bysant spec */
    private List<Object> decodeList(final ListEncoding encoding, final ByteBuffer buffer) throws DecoderException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("decodeList( encoding = {}, buffer = {}", encoding, dumpBuffer(buffer));
        }
        // read the OPCODE
        final int opCode = buffer.get() & ONE_BYTE_MASK;
        LOG.trace("OPCODE : 0x{}", Integer.toHexString(opCode));
        if (opCode == NULL) {
            LOG.trace("decodeList done");
            return null;
        } else if (opCode == encoding.emptyListOpCode()) {
            LOG.trace("decodeList done");
            return Collections.emptyList();
        } else if (encoding.tinyUntypedListOpCode() <= opCode && opCode < encoding.largeUntypedListOpCode()) {
            final int listSize = opCode - encoding.tinyUntypedListOpCode() + 1;
            LOG.trace("tiny list of {} elements", listSize);
            final List<Object> list = new ArrayList<Object>(listSize);
            for (int i = 0; i < listSize; i++) {
                list.add(decodeOne(BysantContext.GLOBAL.getEncoding(), buffer));
            }
            LOG.trace("decodeList done");
            return list;
        } else if (opCode == encoding.largeUntypedListOpCode()) {
            final int listSize = decodeUint((UintEncoding) BysantContext.UINTS_AND_STRS.getEncoding(), buffer)
                    .intValue() + encoding.tinyListLimit() + 1;
            LOG.trace("large untyped list of {} elements", listSize);
            final List<Object> list = new ArrayList<Object>(listSize);
            for (int i = 0; i < listSize; i++) {
                list.add(decodeOne(BysantContext.GLOBAL.getEncoding(), buffer));
            }
            LOG.trace("decodeList done");
            return list;
        } else if (opCode == encoding.nullTerminatedUntypedListOpCode()) {
            LOG.trace("null terminated untyped list");
            final List<Object> list = new ArrayList<Object>();
            for (;;) {
                final Object obj = decodeOne(BysantContext.GLOBAL.getEncoding(), buffer);
                if (obj == null) {
                    break;
                }
                list.add(obj);
            }
            LOG.trace("decodeList done");
            return list;
        } else if (encoding.tinyTypedListOpCode() <= opCode && opCode < encoding.largeTypedListOpCode()) {
            final int listSize = opCode - encoding.tinyTypedListOpCode() + 1;
            LOG.trace("typed tiny list of {} elements", listSize);
            final List<Object> list = new ArrayList<Object>();
            BysantContext subCtx = getEncodedContext(buffer);
            for (int i = 0; i < listSize; i++) {
                list.add(decodeOne(subCtx.getEncoding(), buffer));
            }
            LOG.trace("decodeList done");
            return list;
        } else if (opCode == encoding.largeTypedListOpCode()) {
            final int listSize = decodeUint((UintEncoding) BysantContext.UINTS_AND_STRS.getEncoding(), buffer)
                    .intValue() + encoding.tinyListLimit() + 1;
            LOG.trace("typed large list of {} elements", listSize);
            final List<Object> list = new ArrayList<Object>();
            BysantContext subCtx = getEncodedContext(buffer);
            for (int i = 0; i < listSize; i++) {
                list.add(decodeOne(subCtx.getEncoding(), buffer));
            }
            LOG.trace("decodeList done");
            return list;
        } else if (opCode == encoding.nullTerminatedTypedListOpCode()) {
            LOG.trace("null terminated typed list");
            final List<Object> list = new ArrayList<Object>();
            BysantContext subCtx = getEncodedContext(buffer);
            for (;;) {
                final Object obj = decodeOne(subCtx.getEncoding(), buffer);
                if (obj == null) {
                    break;
                }
                list.add(obj);
            }
            LOG.trace("decodeList done");
            return list;
        } else {
            throw new DecoderException("this is not a list OPCODE : " + opCode);
        }
    }

    private BysantContext getEncodedContext(final ByteBuffer buffer) throws DecoderException {
        int ctxId = buffer.get() & ONE_BYTE_MASK;
        BysantContext subCtx = BysantContext.findById(ctxId);

        if (subCtx == null) {
            throw new DecoderException("unsupported context id : " + ctxId);
        }
        return subCtx;
    }

    /** decode a map following the bysant spec */
    private Map<Object, Object> decodeMap(final MapEncoding encoding, final ByteBuffer buffer) throws DecoderException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("decodeMap( encoding = {}, buffer = {}", encoding, dumpBuffer(buffer));
        }
        // read the OPCODE
        final int opCode = buffer.get() & ONE_BYTE_MASK;
        LOG.trace("OPCODE : 0x{}", Integer.toHexString(opCode));
        if (opCode == NULL) {
            return null;
        } else if (opCode >= encoding.tinyUntypedMapOpCode()
                && opCode < (encoding.tinyUntypedMapOpCode() + encoding.tinyMapLimit())) {
            // untyped tiny map

            final int mapSize = opCode - encoding.tinyUntypedMapOpCode() + 1;
            LOG.trace("untyped tiny map size = {}", mapSize);
            return decodeUntypedMapBody(buffer, mapSize);
        } else if (opCode == encoding.largeUntypedMapOpCode()) {
            // untyped large map
            final int mapSize = decodeUint((UintEncoding) BysantContext.UINTS_AND_STRS.getEncoding(), buffer)
                    .intValue() + encoding.tinyMapLimit() + 1;
            LOG.trace("untyped large map size = {}", mapSize);
            return decodeUntypedMapBody(buffer, mapSize);
        } else if (opCode == encoding.nullTerminatedUntypedMapOpCode()) {
            LOG.trace("null terminated untyped map");
            final Map<Object, Object> map = new HashMap<Object, Object>();
            for (;;) {
                // read key
                Object key = decodeOne(BysantContext.UINTS_AND_STRS.getEncoding(), buffer);
                if (key == null) {
                    break;
                }
                if (key instanceof ByteBuffer) {
                    // convert as string, we don't give a shit about binary map key
                    try {
                        key = new String(((ByteBuffer) key).array(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException(e);
                    }
                }

                // read object
                final Object obj = decodeOne(BysantContext.GLOBAL.getEncoding(), buffer);

                map.put(key, obj);
            }
            return map;
        } else if (opCode == encoding.largeTypedMapOpCode()) {
            // typed map (TODO)
            throw new IllegalStateException("not implemented : large typed map");
        } else if (opCode >= encoding.tinyTypedMapOpCode()
                && opCode < (encoding.tinyTypedMapOpCode() + encoding.tinyMapLimit())) {
            // typed tiny map (TODO)
            throw new IllegalStateException("not implemented : tiny typed map");
        } else if (opCode == encoding.nullTerminatedTypedMapOpCode()) {
            // null terminated typed map (TODO)
            throw new IllegalStateException("not implemented : null terminated typed map");
        } else if (opCode == encoding.emptyMapOpCode()) {
            LOG.trace("empty map");
            // an empty map
            return Collections.emptyMap();
        } else if (opCode == NULL) {
            LOG.trace("NULL map");
            // a null map
            return null;
        } else {
            throw new DecoderException("this is not a map OPCODE : " + opCode);
        }
    }

    /** decode a map body of untyped pair following the bysant spec */
    private Map<Object, Object> decodeUntypedMapBody(final ByteBuffer buffer, final int mapSize)
            throws DecoderException {
        final Map<Object, Object> map = new HashMap<Object, Object>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            // read key
            LOG.trace("decode key");
            Object key = decodeOne(BysantContext.UINTS_AND_STRS.getEncoding(), buffer);
            if (key instanceof ByteBuffer) {
                // convert as string, we don't give a shit about binary map key
                try {
                    key = new String(((ByteBuffer) key).array(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
            }

            // read object
            LOG.trace("decode value");
            final Object obj = decodeOne(BysantContext.GLOBAL.getEncoding(), buffer);
            LOG.trace("KEY '{}' => VALUE '{}'", key, obj);
            map.put(key, obj);
        }
        return map;
    }

    /** decode a AWTDA object following the bysant spec */
    private Object decodeAwt(final M3daEncoding encoding, final ByteBuffer buffer) throws DecoderException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("decodeAwt( encoding = {}, buffer = {}", encoding, dumpBuffer(buffer));
        }
        // read the OPCODE
        final int opCode = buffer.get() & ONE_BYTE_MASK;
        LOG.trace("OPCODE : 0x{}", Integer.toHexString(opCode));
        if (opCode == NULL) {
            LOG.trace("null");
            return null;
        } else if (opCode == encoding.getDeltaVectorOpCode()) {
            LOG.trace("delta vector");
            final Number factor = decodeNumber((NumberEncoding) BysantContext.NUMBERS.getEncoding(), buffer);
            final Number start = decodeNumber((NumberEncoding) BysantContext.NUMBERS.getEncoding(), buffer);
            final List<Object> deltas = decodeList((ListEncoding) BysantContext.LIST_AND_MAPS.getEncoding(), buffer);
            final List<Number> typedDeltas = new ArrayList<Number>(deltas.size());
            for (int i = 0; i < deltas.size(); i++) {
                final Object value = deltas.get(i);
                if (!(value instanceof Number)) {
                    throw new DecoderException("deltas of a deltas vector should contain only Number, not "
                            + value.getClass().getCanonicalName());
                }
                typedDeltas.add((Number) value);
            }
            return new M3daDeltasVector(factor, start, typedDeltas);
        } else if (opCode == encoding.getEnvelopeOpCode()) {
            LOG.trace("envelope");
            final Map<Object, Object> header = decodeMap((MapEncoding) BysantContext.LIST_AND_MAPS.getEncoding(),
                    buffer);
            LOG.trace("extracting payload string");
            final ByteBuffer payload = decodeStringAsBytes(
                    (UintStrCtxEncoding) BysantContext.UINTS_AND_STRS.getEncoding(), buffer);
            if (LOG.isTraceEnabled()) {
                LOG.trace("payload of {} bytes found : {} ", payload == null ? null : payload.remaining(), payload);
            }

            final Map<Object, Object> footer = decodeMap((MapEncoding) BysantContext.LIST_AND_MAPS.getEncoding(),
                    buffer);
            return new M3daEnvelope(header == null ? Collections.emptyMap() : header, payload == null ? new byte[] {}
                    : payload.array(), footer == null ? Collections.emptyMap() : footer);
        } else if (opCode == encoding.getMessageOpCode()) {
            LOG.trace("message");

            String path;
            try {
                path = new String(decodeStringAsBytes((StringEncoding) BysantContext.UINTS_AND_STRS.getEncoding(),
                        buffer).array(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }

            final Number ticket = decodeUint((UintEncoding) BysantContext.UINTS_AND_STRS.getEncoding(), buffer);
            final Map<Object, Object> body = decodeMap((MapEncoding) BysantContext.LIST_AND_MAPS.getEncoding(), buffer);
            return new M3daMessage(path, ticket == null ? null : ticket.longValue(), body);
        } else if (opCode == encoding.getQuasiPeriodicVectorOpCode()) {
            LOG.trace("quasi periodic vector op code");
            final Number period = decodeNumber((NumberEncoding) BysantContext.NUMBERS.getEncoding(), buffer);
            final Number start = decodeNumber((NumberEncoding) BysantContext.NUMBERS.getEncoding(), buffer);
            final List<Object> shifts = decodeList((ListEncoding) BysantContext.LIST_AND_MAPS.getEncoding(), buffer);
            final List<Number> typedShifts = new ArrayList<Number>(shifts.size());
            for (int i = 0; i < shifts.size(); i++) {
                final Object value = shifts.get(i);
                if (!(value instanceof Number)) {
                    throw new DecoderException("shifts of a quasi periodic vector should contain only Number, not "
                            + value.getClass().getCanonicalName());
                }
                typedShifts.add((Number) value);
            }
            return new M3daQuasiPeriodicVector(period, start, typedShifts);
        } else if (opCode == encoding.getResponseOpCode()) {
            LOG.trace("response");
            final Long ticketId = decodeUint((UintEncoding) BysantContext.UINTS_AND_STRS.getEncoding(), buffer)
                    .longValue();
            final Long status = decodeNumber((NumberEncoding) BysantContext.NUMBERS.getEncoding(), buffer).longValue();

            String data;
            try {
                final ByteBuffer dataBin = decodeStringAsBytes(
                        (StringEncoding) BysantContext.UINTS_AND_STRS.getEncoding(), buffer);
                if (dataBin == null) {
                    data = null;
                } else {
                    data = new String(dataBin.array(), "UTF-8");
                }
            } catch (final UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
            return new M3daResponse(ticketId, status, data);
        } else {
            throw new DecoderException("this is not a AWT message OPCODE : " + opCode);
        }
    }

    /** helper for generating an hexa-decimal dump of a ByteBuffer */
    private static final String dumpBuffer(final ByteBuffer buffer) {
        final byte[] data = new byte[buffer.remaining()];
        final int pos = buffer.position();
        buffer.get(data);
        buffer.position(pos);
        return Hex.encodeHexString(data);
    }
}
