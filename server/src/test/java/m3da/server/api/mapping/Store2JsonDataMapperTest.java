/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package m3da.server.api.mapping;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.store.DataValue;
import m3da.server.store.Envelope;
import m3da.server.store.Message;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

public class Store2JsonDataMapperTest {

	Store2JsonDataMapper mapper;

	@Before
	public void setUp() {
		this.mapper = new Store2JsonDataMapper();

	}

	private Map<Long, Envelope> makeEnvelope(Map<String, List<DataValue<?>>> messsageData,
			long now) {
		Map<Long, Envelope> lastReceived = new HashMap<Long, Envelope>();
		Long nanoseconds = now * 1000;
		lastReceived.put(nanoseconds, new Envelope(now, Arrays.asList(new Message("@sys.foo", messsageData))));
		return lastReceived;
	}

	private void addSimpleMessage(Map<Long, Envelope> lastReceived, Long comm1, Integer value) {
		Map<String, List<DataValue<?>>> messageData = new HashMap<String, List<DataValue<?>>>();
		List<DataValue<?>> dataValues = new ArrayList<DataValue<?>>();
		dataValues.add(new DataValue<Integer>(comm1, value));
		messageData.put("bar", dataValues);
		Message message1 = new Message("@sys.foo", messageData);
		lastReceived.put(comm1, new Envelope(comm1, Arrays.asList(message1)));
	}
	
	@Test
	public void get_maps_single_received_data() {

		long now = System.currentTimeMillis();

		Map<String, List<DataValue<?>>> messageData = new HashMap<String, List<DataValue<?>>>();
		List<DataValue<?>> dataValues = new ArrayList<DataValue<?>>();
		dataValues.add(new DataValue<Integer>(now, 42));
		messageData.put("bar", dataValues);

		Map<Long, Envelope> lastReceived = makeEnvelope(messageData, now);

		Map<String, List<JSystemReadData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemReadData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals(42, bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}

	@Test
	public void get_converts_byte_buffers_to_utf8_string() {
		final long now = System.currentTimeMillis();
		
		Map<String, List<DataValue<?>>> messageData = new HashMap<String, List<DataValue<?>>>();
		List<DataValue<?>> dataValues = new ArrayList<DataValue<?>>();
		dataValues.add(new DataValue<ByteBuffer>(now, ByteBuffer.wrap("toto".getBytes())));
		messageData.put("bar", dataValues);
		Map<Long, Envelope> lastReceived = makeEnvelope(messageData, now);

		Map<String, List<JSystemReadData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemReadData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals("toto", bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}


	
	@Test
	public void get_maps_several_received_data() {
		long now = System.currentTimeMillis();

		Map<String, List<DataValue<?>>> envelopeData = new HashMap<String, List<DataValue<?>>>();
		List<DataValue<?>> dataValues1 = new ArrayList<DataValue<?>>();
		dataValues1.add(new DataValue<Integer>(now, 42));
		envelopeData.put("bar", dataValues1);
		
		List<DataValue<?>> dataValues2 = new ArrayList<DataValue<?>>();
		dataValues2.add(new DataValue<String>(now, "Hello"));
		envelopeData.put("baz", dataValues2);
		
		Map<Long, Envelope> lastReceived = makeEnvelope(envelopeData, now);

		Map<String, List<JSystemReadData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemReadData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals(42, bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

		JSystemReadData baz = mapped.get("@sys.foo.baz").get(0);
		assertEquals(1, baz.getValue().size());
		assertEquals("Hello", baz.getValue().get(0));
		assertEquals(String.valueOf(now), baz.getTimestamp());

	}

	@Test
	public void get_collects_successive_communications() {
		long now = System.currentTimeMillis();
		
		Map<Long, Envelope> lastReceived = new HashMap<Long, Envelope>();

		Long comm1 = now;
		addSimpleMessage(lastReceived, comm1, 42);

		Long comm2 = (now + 5000);
		addSimpleMessage(lastReceived, comm2, 43);
		
		// Results should be sorted by decreasing timestamps
		Map<String, List<JSystemReadData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemReadData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals(43, bar.getValue().get(0));
		assertEquals(String.valueOf(now + 5000), bar.getTimestamp());

		bar = mapped.get("@sys.foo.bar").get(1);
		assertEquals(1, bar.getValue().size());
		assertEquals(42, bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}

	
	@Test
	public void get_handles_null_results() {
		Map<String, List<JSystemReadData>> mapped = mapper.mapReceivedData(null);
		assertEquals(0, mapped.keySet().size());
	}

	@Test
	public void post_converts_data_to_m3da_message() {

		JSystemWriteSettings settings = new JSystemWriteSettings(null);
		settings.setSettings(Arrays.asList(new JSystemWriteData("@sys.greenhouse.humidity", 42),
				new JSystemWriteData("@sys.greenhouse.msg", "hello"), new JSystemWriteData("@sys.foo.bar", 12)));

		List<Message> dataToSend = mapper.mapDataToSend(settings);

		assertEquals(2, dataToSend.size());

		Message message = dataToSend.get(0);
		assertEquals("@sys.greenhouse", message.getPath());
		Map<String, List<DataValue<?>>> data = message.getData();
		assertEquals(42, data.get("humidity").get(0).getValue());

		ByteBuffer msg = (ByteBuffer) data.get("msg").get(0).getValue();
		assertEquals("hello", new String(msg.array(), Charset.forName("utf8")));

		message = dataToSend.get(1);
		assertEquals("@sys.foo", message.getPath());
		data = message.getData();
		assertEquals(12, data.get("bar").get(0).getValue());

	}
}
