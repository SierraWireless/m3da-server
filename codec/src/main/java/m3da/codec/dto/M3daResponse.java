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

/**
 * A response PDU used to used for acknowledging a message.
 */
public class M3daResponse implements M3daBodyMessage {

    private final Long ticketId;
    private final Long status;
    private final String message;

    public M3daResponse(Long ticketId, Long status, String message) {
        super();
        this.ticketId = ticketId;
        this.status = status;
        this.message = message;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public Long getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "M3daResponse [ticketId=" + ticketId + ", status=" + status + ", message=" + message + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((ticketId == null) ? 0 : ticketId.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        M3daResponse other = (M3daResponse) obj;
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
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
