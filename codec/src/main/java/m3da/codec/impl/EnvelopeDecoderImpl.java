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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import m3da.codec.BysantDecoder;
import m3da.codec.DecoderException;
import m3da.codec.DecoderOutput;
import m3da.codec.EnvelopeDecoder;
import m3da.codec.dto.M3daEnvelope;
import m3da.codec.dto.M3daPdu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link EnvelopeDecoder}. Not thread safe for performance reason : instantiate one for each thread/session/device and push the
 * byte buffer as they come.
 * 
 */
public class EnvelopeDecoderImpl implements EnvelopeDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(EnvelopeDecoderImpl.class);

    private final BysantDecoder enveloppeDecoder = new BysantDecoderImpl();

    private List<M3daEnvelope> decodedEnvelope = new ArrayList<M3daEnvelope>(2);

    private boolean someParasite = false;

    private Object parasite = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void decodeAndAccumulate(ByteBuffer buffer, final DecoderOutput<M3daEnvelope> output)
            throws DecoderException {
        if (decodedEnvelope.size() > 0) {

            throw new IllegalStateException("already used decoder, instanciate a new one for your session");
        }
        parasite = null;
        // decode the envelope
        try {
            enveloppeDecoder.decodeAndAccumulate(buffer, new DecoderOutput<M3daPdu>() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void decoded(M3daPdu pdu) {
                    if (pdu instanceof M3daEnvelope) {
                        decodedEnvelope.add((M3daEnvelope) pdu);
                    } else {
                        decodedEnvelope = new ArrayList<M3daEnvelope>(2);
                        parasite = pdu;
                        someParasite = true;
                    }
                }
            });
        } catch (ClassCastException e) {
            throw new DecoderException("could not decode the envelope", e);
        }
        if (parasite != null) {
            throw new DecoderException("no envelope found in this message, but a : "
                    + parasite.getClass().getCanonicalName());
        }
        // decode the envelope content
        if (decodedEnvelope != null) {
            for (M3daEnvelope e : decodedEnvelope) {
                decodedEnvelope = null;
                output.decoded(e);
            }
            decodedEnvelope = new ArrayList<M3daEnvelope>(2);
        } else {
            LOG.debug("accumulating more bytes");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishDecode() throws DecoderException {
        enveloppeDecoder.finishDecode();
        if (someParasite) {
            throw new DecoderException("trailling data : " + parasite);
        }
    }
}
