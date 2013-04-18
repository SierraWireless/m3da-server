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
package m3da.server.session;

/**
 * The cipher algorithm to be used for M3DA with AUTH_HMAC security scheme : 'none', 'aes-cbc-128', 'aes-cbc-256',
 * 'aes-ctr-128', 'aes-ctr-256'.
 */
public enum M3daCipher {

    NONE(null), //
    AES_CTR_256("aes-ctr-256"), //
    AES_CTR_128("aes-ctr-128"), //
    AES_CBC_256("aes-cbc-256"), //
    AES_CBC_128("aes-cbc-128");

    private final String description;

    private M3daCipher(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
