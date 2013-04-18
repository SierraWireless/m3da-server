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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import m3da.server.store.impl.InMemoryStore;

import org.junit.Test;

/**
 * Unit test for {@link InMemoryStore}
 */
public class InMemoryStoreServiceTest {

	private InMemoryStore service = new InMemoryStore(3);

	@Test
	public void enqueue_3_message_get_3_message() {

		// prepare
		List<Message> msgA = new ArrayList<Message>(1);
		List<Message> msgB = new ArrayList<Message>(1);
		List<Message> msgC = new ArrayList<Message>(1);

		// run
		service.enqueueReceivedData("clientId", 1, new Envelope(1L, msgA));
		service.enqueueReceivedData("clientId", 2, new Envelope(2L, msgB));
		service.enqueueReceivedData("clientId", 3, new Envelope(3L, msgC));

		// verify
		Map<Long, Envelope> data = service.lastReceivedData("clientId");
		assertEquals(3, data.size());
		assertEquals(msgA, data.get(1L).getMessages());
		assertEquals(Long.valueOf(1L), data.get(1L).getReceptionTime());

		assertEquals(msgB, data.get(2L).getMessages());
		assertEquals(Long.valueOf(2L), data.get(2L).getReceptionTime());

		assertEquals(msgC, data.get(3L).getMessages());
		assertEquals(Long.valueOf(3L), data.get(3L).getReceptionTime());

	}

	@Test
	public void enqueue_4_message_get_3_message_the_oldest_one_is_discarded() {

		// prepare
		List<Message> msgA = new ArrayList<Message>(1);
		List<Message> msgB = new ArrayList<Message>(1);
		List<Message> msgC = new ArrayList<Message>(1);
		List<Message> msgD = new ArrayList<Message>(1);

		// run
		service.enqueueReceivedData("clientId", 1, new Envelope(1L, msgA));
		service.enqueueReceivedData("clientId", 2, new Envelope(2L, msgB));
		service.enqueueReceivedData("clientId", 3, new Envelope(3L, msgC));
		service.enqueueReceivedData("clientId", 4, new Envelope(4L, msgD));

		// verify
		Map<Long, Envelope> data = service.lastReceivedData("clientId");
		assertEquals(3, data.size());
		assertEquals(msgB, data.get(2L).getMessages());
		assertEquals(Long.valueOf(2L), data.get(2L).getReceptionTime());

		assertEquals(msgC, data.get(3L).getMessages());
		assertEquals(Long.valueOf(3L), data.get(3L).getReceptionTime());

		assertEquals(msgD, data.get(4L).getMessages());
		assertEquals(Long.valueOf(4L), data.get(4L).getReceptionTime());

	}

	@Test
	public void push_some_message_and_gather_them_in_order() {

		// prepare
		List<Message> msgs = new ArrayList<Message>(3);

		msgs.add(new Message("path1", null));
		msgs.add(new Message("path2", null));
		msgs.add(new Message("path3", null));

		// run
		service.enqueueDataToSend("clientId", msgs);

		// verify

		assertNull(service.popDataToSend("unknownClient"));
		List<Message> popedMsgs = service.popDataToSend("clientId");
		assertEquals(3, popedMsgs.size());
		assertEquals("path1", popedMsgs.get(0).getPath());
		assertEquals("path2", popedMsgs.get(1).getPath());
		assertEquals("path3", popedMsgs.get(2).getPath());
	}

	@Test
	public void list_clients() {
		// prepare
		List<Message> msgA = new ArrayList<Message>(1);
		List<Message> msgB = new ArrayList<Message>(1);
		List<Message> msgC = new ArrayList<Message>(1);
		List<Message> msgD = new ArrayList<Message>(1);

		// run
		service.enqueueReceivedData("clientId1", 1, new Envelope(1L, msgA));
		service.enqueueReceivedData("clientId1", 2, new Envelope(2L, msgB));
		service.enqueueReceivedData("clientId2", 3, new Envelope(3L, msgC));
		service.enqueueReceivedData("clientId2", 4, new Envelope(4L, msgD));

		// run
		service.enqueueDataToSend("clientId2", msgA);
		service.enqueueDataToSend("clientId3", msgB);

		Set<String> inClientIds = service.incomingClientIds();
		assertEquals(2, inClientIds.size());
		assertTrue(inClientIds.contains("clientId1"));
		assertTrue(inClientIds.contains("clientId2"));
		
		Set<String> outClientIds = service.outgoingClientIds();
		assertEquals(2, outClientIds.size());
		assertTrue(outClientIds.contains("clientId2"));
		assertTrue(outClientIds.contains("clientId3"));
		
	}
	
	

}
