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
import m3da.server.store.DataValue;
import m3da.server.store.Envelope;
import m3da.server.store.Message;

/**
 * Mapping between m3da.server.store beans and JSON representations.
 */
public class Store2JsonDataMapper {

    /**
     * Maps a hashmap of received data (organized by reception time, with a list of m3da.store.bean.Messages) into a map of
     * JSystemData, organized by data id.
     * 
     * @param lastReceived
     */
    public Map<String, List<JSystemReadData>> mapReceivedData(Map<Long, Envelope> data) {

        Map<String, List<JSystemReadData>> jDataById = new HashMap<String, List<JSystemReadData>>();

        if (data == null) {
            return jDataById;
        }

        for (Map.Entry<Long, Envelope> e : data.entrySet()) {

            Envelope envelope = e.getValue();
            String timestampInMs = String.valueOf(envelope.getReceptionTime());

            for (Message message : envelope.getMessages()) {

                String path = message.getPath();
                Map<String, List<DataValue<?>>> pathData = message.getData();

                for (Map.Entry<String, List<DataValue<?>>> received : pathData.entrySet()) {

                    String key = received.getKey();
                    String dataId = path + "." + key;
                    List<DataValue<?>> dataValues = received.getValue();

                    List<JSystemReadData> jData = null;
                    if (jDataById.containsKey(dataId)) {
                        jData = jDataById.get(dataId);
                    } else {
                        jData = new ArrayList<JSystemReadData>();
                        jDataById.put(dataId, jData);
                    }
                    
                    for (DataValue<?> dataValue : dataValues) {
                        JSystemReadData jSystemData = new JSystemReadData();
                        jSystemData.setTimestamp(String.valueOf(dataValue.getTimestamp()));
                        
    					jSystemData.setValue(this.byteBuffer2String(dataValue.getValue()));
                        jData.add(jSystemData);
                    }
                }

            }

        }

        for (Map.Entry<String, List<JSystemReadData>> resEntry : jDataById.entrySet()) {
            this.sortJSystemDataList(resEntry.getValue());
        }

        return jDataById;

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
    private List<Object> byteBuffers2Strings(List<DataValue<?>> values) {

        List<Object> res = new ArrayList<Object>();

        for (DataValue<?> dataValue : values) {
        	Object o = dataValue.getValue();
            if (o instanceof ByteBuffer) {
                String str = new String(((ByteBuffer) o).array(), Charset.forName("utf-8"));
                res.add(str);
            } else {
                res.add(o);
            }
        }

        return res;
    }

    private List<Object> byteBuffer2String(Object o) {
    	List<Object> res = new ArrayList<Object>();
    	if (o instanceof ByteBuffer) {
            String str = new String(((ByteBuffer) o).array(), Charset.forName("utf-8"));
            res.add(str);
        } else {
            res.add(o);
        }
    	return res;
    }
    
    /**
     * @param settings
     * @return
     */
    public List<Message> mapDataToSend(JSystemWriteSettings settings) {

    	long now = System.currentTimeMillis();
        Map<String, Message> messagesByPath = new HashMap<String, Message>();

        for (JSystemWriteData writeData : settings.getSettings()) {

            String key = writeData.getKey();

            int lastDot = key.lastIndexOf(".");
            if (lastDot != -1) {
                String path = key.substring(0, lastDot);
                String id = key.substring(lastDot + 1);

                Map<String, List<DataValue<?>>> data = null;
                if (messagesByPath.containsKey(path)) {
                    data = messagesByPath.get(path).getData();
                } else {
                    data = new HashMap<String, List<DataValue<?>>>();
                    Message message = new Message(path, data);
                    messagesByPath.put(path, message);
                }
                
                // TODO(pht) should we create this list of dataValues every time ?
                List<DataValue<?>> dataValues = new ArrayList<DataValue<?>>();
                
                // "String" values must be translated to ByteBuffer before
                // being put in the message
                Object value = string2ByteBuffer(writeData.getValue());
                // All "outgoing" data will be timestamped with the reception of 
                // the JSON message
                DataValue<Object> dataValue = new DataValue<Object>(now, value);
				dataValues.add(dataValue);
                
				data.put(id, dataValues);
                // data.put(id, Arrays.asList(string2ByteBuffer(writeData.getValue())));

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
