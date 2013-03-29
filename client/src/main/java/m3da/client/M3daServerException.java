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
package m3da.client;

import m3da.codec.StatusCode;

/**
 * Error received sent by the server.
 */
@SuppressWarnings("serial")
public class M3daServerException extends Exception {

    private final StatusCode statusCode;

    public M3daServerException(StatusCode statusCode) {
        super("status " + statusCode.name() + " (" + statusCode.getCode() + ")");
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
