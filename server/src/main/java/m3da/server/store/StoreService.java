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
import java.util.Map;
import java.util.Set;

/**
 * Service for storing and querying the received data by client identifier
 */
public interface StoreService {

	/**
	 * Enqueue some received data from the client
	 * 
	 * @param clientId
	 *            the client unique identifier ("ID" in the M3DA header)
	 * @param receptionInNanoSec
	 *            the reception time in nanoseconds (see {@link System#nanoTime()}
	 * @param newData
	 *            the received M3DA data
	 */
	public void enqueueReceivedData(String clientId, long receptionInNanoSec, Envelope newData);

	/**
	 * Get the last received data for a given client
	 * 
	 * @param clientId
	 *            the client unique identifier
	 * @return the last N received data from the given client
	 */
	public Map<Long, Envelope> lastReceivedData(String clientId);

	/**
	 * add some data to be sent to the client, as soon it reconnect to the server.
	 * 
	 * @param clientId
	 *            the identifier of the client targeted by those data
	 * @param newData
	 *            the list of data to be sent
	 */
	public void enqueueDataToSend(String clientId, List<Message> newData);

	/**
	 * Get and remove the data to be sent to a client.
	 * 
	 * @param clientId
	 *            the identifier of the client
	 * @return the data to be sent, or <code>null</code>
	 */
	public List<Message> popDataToSend(String clientId);

	/**
	 * Get the clients ids for which data was received.
	 * 
	 * @return list of client ids 
	 */
	public Set<String> incomingClientIds();
	
	/**
 	 * Get the clients ids for which data is ready to be sent.
 	 * 
	 * @return list of client ids
	 */
	public Set<String> outgoingClientIds();
	
	
	/**
	 * start the service
	 */
	public void start();

	/**
	 * stop the service
	 */
	public void stop();
}
