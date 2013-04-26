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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import m3da.server.api.json.JSystemReadData;
import m3da.server.api.json.JSystemWriteData;
import m3da.server.api.json.JSystemWriteSettings;
import m3da.server.store.Envelope;
import m3da.server.store.Message;

/**
 * Mapping between m3da.server.store beans and JSON representations.
 */
public class Store2JsonDataMapper {

    /**
     * Maps a hashmap of received data (organized by nanoseconds, with a list of m3da.store.bean.Messages) into a map of
     * JSystemData, organized by data id.
     * 
     * Time stamps are converted from nanoseconds (m3da message timestamp) to milliseconds (JSON timestamps.)
     * 
     * @param lastReceived
     */
    public Map<String, List<JSystemReadData>> mapReceivedData(Map<Long, Envelope> data) {

        Map<String, List<JSystemReadData>> res = new HashMap<String, List<JSystemReadData>>();

        if (data == null) {
            return res;
        }

        for (Map.Entry<Long, Envelope> e : data.entrySet()) {

            Envelope envelope = e.getValue();
            String timestampInMs = String.valueOf(envelope.getReceptionTime());

            for (Message message : envelope.getMessages()) {

                String path = message.getPath();
                Map<String, List<?>> pathData = message.getData();

                for (Map.Entry<String, List<?>> received : pathData.entrySet()) {

                    String key = received.getKey();

                    String dataId = path + "." + key;
                    List<JSystemReadData> resData = null;
                    if (res.containsKey(dataId)) {
                        resData = res.get(dataId);
                    } else {
                        resData = new ArrayList<JSystemReadData>();
                        res.put(dataId, resData);
                    }

                    JSystemReadData jSystemData = new JSystemReadData();
                    jSystemData.setTimestamp(timestampInMs);

                    jSystemData.setValue(this.byteBuffers2Strings(received.getValue()));

                    resData.add(jSystemData);

                }

            }

        }

        for (Map.Entry<String, List<JSystemReadData>> resEntry : res.entrySet()) {
            this.sortJSystemDataList(resEntry.getValue());
        }

        return res;

    }

    /**
     * Sort a list of JSystemData, by decreasing time stamps.
     * 
     * @param jSystemDataList
     */
    private void sortJSystemDataList(List<JSystemReadData> jSystemDataList) {
        Comparator<JSystemReadData> comp = new Comparator<JSystemReadData>() {
            @Override
            public int compare(JSystemReadData data1, JSystemReadData data2) {
                // data2 first to get decreasing timestamps
                return (data2.getTimestamp().compareTo(data1.getTimestamp()));
            }
        };
        Collections.sort(jSystemDataList, comp);
    }

    /**
     * "Strings" from m3da are actually ByteBuffers ; we will assume all of them are utf-8 string.
     * 
     * @param values
     * @return the list of values, with ByteBuffers converted to utf-8 strings
     */
    private List<Object> byteBuffers2Strings(List<?> values) {

        List<Object> res = new ArrayList<Object>();

        for (Object o : values) {
            if (o instanceof ByteBuffer) {
                String str = new String(((ByteBuffer) o).array(), Charset.forName("utf-8"));
                res.add(str);
            } else {
                res.add(o);
            }
        }

        return res;
    }

    /**
     * @param settings
     * @return
     */
    public List<Message> mapDataToSend(JSystemWriteSettings settings) {

        Map<String, Message> messagesByPath = new HashMap<String, Message>();

        for (JSystemWriteData writeData : settings.getSettings()) {

            String key = writeData.getKey();

            int lastDot = key.lastIndexOf(".");
            if (lastDot != -1) {
                String path = key.substring(0, lastDot);
                String id = key.substring(lastDot + 1);

                Map<String, List<?>> data = null;
                if (messagesByPath.containsKey(path)) {
                    data = messagesByPath.get(path).getData();
                } else {
                    data = new HashMap<String, List<?>>();
                    Message message = new Message(path, data);
                    messagesByPath.put(path, message);
                }
                data.put(id, Arrays.asList(string2ByteBuffer(writeData.getValue())));

            }

        }

        List<Message> res = new ArrayList<Message>();
        res.addAll(messagesByPath.values());
        return res;

    }

    private Object string2ByteBuffer(Object o) {

        Charset charset = Charset.forName("utf8");
        CharsetEncoder encoder = charset.newEncoder();

        if (o instanceof String) {
            String s = (String) o;
            CharBuffer charBuffer = CharBuffer.wrap(s.toCharArray());
            try {
                return encoder.encode(charBuffer);
            } catch (CharacterCodingException e) {
                return o;
            }
        } else {
            return o;
        }
    }
}
