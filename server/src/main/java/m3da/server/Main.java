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
package m3da.server;

import m3da.server.api.mapping.Store2JsonDataMapper;
import m3da.server.services.clients.JClientsService;
import m3da.server.services.data.JDataService;
import m3da.server.servlet.ClientsServlet;
import m3da.server.servlet.DataServlet;
import m3da.server.store.impl.InMemoryStore;
import m3da.server.tcp.M3daTcpServer;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application.
 * The Java command that is used for launching should fire this main method.
 * 
 */
public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String webappDirLocation = "src/main/webapp/";

        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        Server server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        InMemoryStore storeService = new InMemoryStore(10);
        Store2JsonDataMapper store2jsonMapper = new Store2JsonDataMapper();
        ObjectMapper jacksonMapper = new ObjectMapper();

        JDataService dataService = new JDataService(storeService, store2jsonMapper);
        JClientsService clientsService = new JClientsService(storeService, storeService);

        ServletHolder dataServletHolder = new ServletHolder(new DataServlet(dataService, jacksonMapper));
        root.addServlet(dataServletHolder, "/data/*");

        ServletHolder clientsServletHolder = new ServletHolder(new ClientsServlet(dataService, clientsService,
                jacksonMapper));
        root.addServlet(clientsServletHolder, "/clients/*");

        server.setHandler(root);

        server.start();

        storeService.start();

        M3daTcpServer tcpServer = new M3daTcpServer(2, 30, 44900, 4, 8, storeService, storeService);
        tcpServer.start();
        server.join();

        tcpServer.stop();
        storeService.stop();

    }
}
