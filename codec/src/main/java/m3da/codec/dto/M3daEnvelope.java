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
package m3da.codec.dto;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import m3da.codec.Hex;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;

/**
 * The M3DA::Envelope object is used for the transport of AWTDA content. The envelope contains a Header and Footer that
 * store the transport related information, and a payload that contains the AWTDA applicative data.
 */
public class M3daEnvelope implements M3daPdu {

    private Map<Object, Object> header;
    private byte[] payload;
    private Map<Object, Object> footer;

    public M3daEnvelope() {

    }

    public M3daEnvelope(final Map<Object, Object> header, final byte[] payload, final Map<Object, Object> footer) {
        super();
        this.header = header;
        this.payload = payload;
        this.footer = footer;
    }

    public Map<Object, Object> getHeader() {
        return header;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Map<Object, Object> getFooter() {
        return footer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AwtDa3Envelope [header=");
        mapToStr(header, builder);
        builder.append(", payload=");
        builder.append(Hex.encodeHexString(payload));
        builder.append(", footer=");
        mapToStr(footer, builder);
        builder.append("]");
        return builder.toString();
    }

    private void mapToStr(final Map<Object, Object> map, final StringBuilder builder) {
        for (final Map.Entry<Object, Object> e : map.entrySet()) {
            builder.append(valuetoStr(e.getKey())).append(" => ").append(valuetoStr(e.getValue())).append(" ");
        }
    }

    private String valuetoStr(final Object value) {
        if (value instanceof ByteBuffer) {
            byte[] v = ((ByteBuffer) value).array();
            String str = new String(v, Charsets.UTF_8);
            if (!StringUtils.isAsciiPrintable(str)) {
                str = Hex.encodeHexString(v);
            }
            return "'" + str + "'";
        } else {
            return value.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((footer == null) ? 0 : footer.hashCode());
        result = prime * result + ((header == null) ? 0 : header.hashCode());
        result = prime * result + Arrays.hashCode(payload);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final M3daEnvelope other = (M3daEnvelope) obj;
        if (footer == null) {
            if (other.footer != null) {
                return false;
            }
        } else if (!footer.equals(other.footer)) {
            return false;
        }
        if (header == null) {
            if (other.header != null) {
                return false;
            }
        } else if (!header.equals(other.header)) {
            return false;
        }
        if (!Arrays.equals(payload, other.payload)) {
            return false;
        }
        return true;
    }
}
