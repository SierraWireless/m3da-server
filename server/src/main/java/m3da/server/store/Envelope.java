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
package m3da.server.store;

import java.util.List;

/**
 * A dated collection of Message received from a client.
 */
public class Envelope {
    private Long receptionTime;
    private List<Message> messages;

    /**
     * Create a message from its reception time and
     * 
     * @param timestamp reception time in milliseconds
     * 
     * @param messages
     */
    public Envelope(Long timestamp, List<Message> messages) {
        this.receptionTime = timestamp;
        this.messages = messages;
    }

    /**
     * Reception timestamp in milliseconds
     * @return
     */
    public Long getReceptionTime() {
        return receptionTime;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
