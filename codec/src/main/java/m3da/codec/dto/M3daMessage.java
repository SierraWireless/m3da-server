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

import java.util.Map;

import com.google.common.base.Joiner;

/**
 * The M3DA::Message object is a container object that enables the data transmission between two peers. See the protocol specification for more
 * details.
 */
public class M3daMessage implements M3daPdu {

	private final String path;

	private final Long ticketId;

	private final Map<Object, Object> body;

	/**
	 * @param path
	 * @param ticketId
	 * @param body
	 */
	public M3daMessage(final String path, final Long ticketId, final Map<Object, Object> body) {
		super();
		this.path = path;
		this.ticketId = ticketId;
		this.body = body;
	}

	public String getPath() {
		return path;
	}

	public Long getTicketId() {
		return ticketId;
	}

	public Map<Object, Object> getBody() {
		return body;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AwtDa3Message [\n     path=" + path + ",\n     ticketId=" + ticketId + ",\n     body= {\n     "
				+ Joiner.on("\n     ").useForNull("<null>").withKeyValueSeparator(" => ").join(body) + "} \n]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((ticketId == null) ? 0 : ticketId.hashCode());
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
		final M3daMessage other = (M3daMessage) obj;
		if (body == null) {
			if (other.body != null) {
				return false;
			}
		} else if (!body.equals(other.body)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (ticketId == null) {
			if (other.ticketId != null) {
				return false;
			}
		} else if (!ticketId.equals(other.ticketId)) {
			return false;
		}
		return true;
	}
}
