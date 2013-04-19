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
package m3da.server.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

public abstract class JsonServlet extends HttpServlet {

    protected ObjectMapper jacksonMapper;

    /**
     * @param jacksonMapper
     */
    public JsonServlet(ObjectMapper jacksonMapper) {
        super();
        this.jacksonMapper = jacksonMapper;
    }

    protected void setResponseContentType(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=utf-8");
    }

}