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
 * The security scheme to apply for protocol M3DA
 */
public enum M3daAuthentication {

    /** no security */
    NONE("none"), //
    /** HMAC MD5 authentication */
    HMAC_MD5("hmac-md5"),
    /** HMAC SHA-1 authentication */
    HMAC_SHA1("hmac-sha1");

    private final String description;

    private M3daAuthentication(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
