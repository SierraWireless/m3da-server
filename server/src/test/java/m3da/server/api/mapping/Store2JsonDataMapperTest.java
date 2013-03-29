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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteData;
import m3da.server.api.json.JSystemWriteSettings;
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

	@Test
	public void get_maps_single_received_data() {

		Map<String, List<?>> data = new HashMap<String, List<?>>();
		data.put("bar", Arrays.asList(42));

		long now = System.currentTimeMillis();

		Map<Long, Envelope> lastReceived = makeEventlope(data, now);

		Map<String, List<JSystemReadData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemReadData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals(42, bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}

	@Test
	public void get_converts_byte_buffers_to_utf8_string() {

		Map<String, List<?>> data = new HashMap<String, List<?>>();
		data.put("bar", Arrays.asList(ByteBuffer.wrap("toto".getBytes())));
		long now = System.currentTimeMillis();
		
		Map<Long, Envelope> lastReceived = makeEventlope(data, now);

		Map<String, List<JSystemReadData>> mapped = mapper.mapReceivedData(lastReceived);
		JSystemReadData bar = mapped.get("@sys.foo.bar").get(0);
		assertEquals(1, bar.getValue().size());
		assertEquals("toto", bar.getValue().get(0));
		assertEquals(String.valueOf(now), bar.getTimestamp());

	}

	private Map<Long, Envelope> makeEventlope(Map<String, List<?>> data,
			long now) {
		Map<Long, Envelope> lastReceived = new HashMap<Long, Envelope>();
		Long nanoseconds = now * 1000;
		lastReceived.put(nanoseconds, new Envelope(now, Arrays.asList(new Message("@sys.foo", data))));
		return lastReceived;
	}

	@Test
	public void get_maps_several_received_data() {
		long now = System.currentTimeMillis();
		Map<String, List<?>> data = new HashMap<String, List<?>>();
		data.put("bar", Arrays.asList(42));
		data.put("baz", Arrays.asList("Hello"));

		Map<Long, Envelope> lastReceived = makeEventlope(data, now);

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
		Map<String, List<?>> data1 = new HashMap<String, List<?>>();
		data1.put("bar", Arrays.asList(42));
		lastReceived.put(comm1, new Envelope(comm1, Arrays.asList(new Message("@sys.foo", data1))));

		Long comm2 = (now + 5000);
		Map<String, List<?>> data2 = new HashMap<String, List<?>>();
		data2.put("bar", Arrays.asList(43));
		lastReceived.put(comm2, new Envelope(comm2, Arrays.asList(new Message("@sys.foo", data2))));

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
		assertEquals(42, message.getData().get("humidity").get(0));

		ByteBuffer msg = (ByteBuffer) message.getData().get("msg").get(0);
		assertEquals("hello", new String(msg.array(), Charset.forName("utf8")));

		message = dataToSend.get(1);
		assertEquals("@sys.foo", message.getPath());
		assertEquals(12, message.getData().get("bar").get(0));

	}

	@Test
	public void foo() throws JsonParseException, JsonMappingException, IOException {
		String str = "{ \"settings\" : [ { \"key\" : \"foo\", \"value\" : \"bar\" } ] }";
		str = "{ \"settings\" : [{\"key\":\"foo\",\"value\":\"bar\"}]}";

		ObjectMapper mapper = new ObjectMapper();
		JSystemWriteSettings readValue = mapper.readValue(str, JSystemWriteSettings.class);

		JSystemWriteData d = readValue.getSettings().get(0);

		System.out.println(d.getKey());

	}
}
