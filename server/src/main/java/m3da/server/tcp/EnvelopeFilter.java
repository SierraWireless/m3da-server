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
package m3da.server.tcp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import m3da.codec.DecoderOutput;
import m3da.codec.EnvelopeDecoder;
import m3da.codec.EnvelopeEncoder;
import m3da.codec.M3daCodecService;
import m3da.codec.dto.M3daEnvelope;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IoFilter} in charge of decoding the M3DA envelopes. It decodes received {@link ByteBuffer} into {@link M3daEnvelope} (with accumulation). It
 * encodes sent {@link M3daEnvelope} into {@link ByteBuffer}.
 */
public class EnvelopeFilter extends IoFilterAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(EnvelopeFilter.class);

	private static final String ENCODER_KEY = "AwtDa3EncoderKey";
	private static final String DECODER_KEY = "AwtDa3DecoderKey";

	/** for encoding/decoding envelopes */
	private M3daCodecService codec;

	public EnvelopeFilter(M3daCodecService codec) {
		this.codec = codec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sessionCreated(final NextFilter nextFilter, final IoSession session) throws Exception {

		// create the needed AWT-DA 3 decoder and encoder
		session.setAttribute(ENCODER_KEY, codec.createEnvelopeEncoder());
		session.setAttribute(DECODER_KEY, codec.createEnvelopeDecoder());
		nextFilter.sessionCreated(session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void messageReceived(final NextFilter nextFilter, final IoSession session, final Object message) throws Exception {
		if (message instanceof IoBuffer) {
			// accumulate that buffer
			final EnvelopeOutput decoderOutput = new EnvelopeOutput();
			 
			((EnvelopeDecoder) session.getAttribute(DECODER_KEY)).decodeAndAccumulate(((IoBuffer) message).buf(), decoderOutput);
			final List<M3daEnvelope> envelopes = decoderOutput.getEnvelope();
                        if (envelopes != null) {
				// write the envelope to the next filter
                                for (M3daEnvelope e : envelopes) {
                                    LOG.debug("decoded one envelope : {}", e);
                                    nextFilter.messageReceived(session, e);
                                }
			} else {
				LOG.debug("no envelope decoded, we need to accumulate more bytes");
			}
		} else {
			LOG.error("We should receive IoBuffer, not {}", message.getClass().getCanonicalName());
			nextFilter.messageReceived(session, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void filterWrite(final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest) throws Exception {
		if (writeRequest.getMessage() instanceof M3daEnvelope) {

			M3daEnvelope response = (M3daEnvelope) writeRequest.getMessage();

			LOG.debug("encoding response : {}", response);

			final ByteBuffer encodedBuffer = ((EnvelopeEncoder) session.getAttribute(ENCODER_KEY)).encode(response);

			nextFilter.filterWrite(session,
					new DefaultWriteRequest(IoBuffer.wrap(encodedBuffer), writeRequest.getFuture(), writeRequest.getDestination()));
		} else {
		        LOG.error("We should send M3daEnvelope, not {}", writeRequest.getMessage().getClass().getCanonicalName());
			nextFilter.filterWrite(session, writeRequest);
		}
	}

	private class EnvelopeOutput implements DecoderOutput<M3daEnvelope> {
	        private List<M3daEnvelope> envelopes = new ArrayList<M3daEnvelope>(2);
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void decoded(final M3daEnvelope pdu) {
		    envelopes.add(pdu);
		}

		public List<M3daEnvelope> getEnvelope() {
			return envelopes;
		}
	}
}
