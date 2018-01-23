/**
 * Copyright (c) 2012 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

/**
 * Redis client binding for YCSB.
 *
 * All YCSB records are mapped to a Redis *hash field*.  For scanning
 * operations, all keys are saved (by an arbitrary hash) in a sorted set.
 */

package com.yahoo.ycsb.db;


import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import com.yahoo.ycsb.StringByteIterator;

import API.clientAPI;
import client.clientMasterSlave;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import utils.Cipher;
import utils.SecurityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * YCSB binding for <a href="http://redis.io/">Redis</a>.
 *
 * See {@code redis/README.md} for details.
 */
public class RedisClient extends DB {

	private clientAPI client;

	public static final String HOST_PROPERTY = "redis.host";
	public static final String PORT_PROPERTY = "redis.port";
	public static final String PASSWORD_PROPERTY = "redis.password";

	public static final String INDEX_KEY = "_indices";

	public static final String ENCRYPTION_PROPERTY = "redis.encryption";

	public void init() throws DBException {
		Properties props = getProperties();
		int port;

		String portString = props.getProperty(PORT_PROPERTY);
		if (portString != null) {
			port = Integer.parseInt(portString);
		} else {
			port = Protocol.DEFAULT_PORT;
		}

		String encryption = props.getProperty(ENCRYPTION_PROPERTY);
		String host = props.getProperty(HOST_PROPERTY);

		Map<String,Cipher> mapping = new HashMap<String,Cipher>();
		mapping.put("field1", Cipher.DET);
		mapping.put("field2", Cipher.DET);
		mapping.put("field3", Cipher.ADD);
		mapping.put("field4", Cipher.MULT);
		mapping.put("field5", Cipher.OPE);
		mapping.put("field6", Cipher.SEARCH);


		//NORMAL
		client= new clientMasterSlave("https://"+host+":"+port+"/");
		if(client == null)
			System.err.println("esta null");

		//ENCRYPTED
		if("E".equals(encryption)) {
			client= new clientMasterSlave("https://"+host+":"+port+"/",SecurityType.ENCRYPTED, "" ,mapping);
		}


		//ENHACED ENCRYPTED
		if("EE".equals(encryption)) {
			client= new clientMasterSlave("https://"+host+":"+port+"/", SecurityType.ENHANCED_ENCRYPTED, "" ,mapping);
		}

	}

	public void cleanup() throws DBException {
		client.Close();
	}

	/*
	 * Calculate a hash for a key to store it in an index. The actual return value
	 * of this function is not interesting -- it primarily needs to be fast and
	 * scattered along the whole space of doubles. In a real world scenario one
	 * would probably use the ASCII values of the keys.
	 */
	private double hash(String key) {
		return key.hashCode();
	}

	// XXX jedis.select(int index) to switch to `table`

	@Override
	public Status read(String table, String key, Set<String> fields,
			Map<String, ByteIterator> result) {
		System.err.println("read redis");
		try {
			if (fields == null) {

				StringByteIterator.putAllAsByteIterators(result, client.getSet(key).get());

			} else {
				for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
					String field = (String) iterator.next();
					;
					result.put(field,new StringByteIterator(client.getElement(key, field).get()));
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.isEmpty() ? Status.ERROR : Status.OK;
	}

	@Override
	public Status insert(String table, String key,
			Map<String, ByteIterator> values) {
		Map<String, String> map = StringByteIterator.getStringMap(values);

		try {
			client.addSet(key, map);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Map.Entry<String, String> pair : map.entrySet()) {
			System.err.println(pair.getKey() + "    " + pair.getValue() + "ola");
		}
		System.err.println();
		System.err.println(key);
		return Status.OK;

	}

	@Override
	public Status delete(String table, String key) {

		try {
			client.removeSet(key);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status update(String table, String key,
			Map<String, ByteIterator> values) {
		Map<String, String> map = StringByteIterator.getStringMap(values);
		for (Map.Entry<String, String> pair : map.entrySet()) {
			client.addElement(key, pair.getKey(), pair.getValue())	;
		}
		return Status.OK;
	}

	@Override
	public Status scan(String table, String startkey, int recordcount,
			Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {

		//		Set<String> keys = jedis.zrangeByScore(INDEX_KEY, hash(startkey),
		//				Double.POSITIVE_INFINITY, 0, recordcount);
		//
		//		HashMap<String, ByteIterator> values;
		//		for (String key : keys) {
		//			values = new HashMap<String, ByteIterator>();
		//			read(table, key, fields, values);
		//			result.add(values);
		//		}

		return Status.OK;
	}
	
	@Override
	public Status elementContainsSentence(String key, String field, String word) {
		// TODO Auto-generated method stub
		try {
			client.elementContainsSentence(key, field, word).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Status searchEntryContainingSentence(String field, String word) {
		// TODO Auto-generated method stub
		try {
			client.searchEntryContainingSentence(field, word).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Status incr(String key, String field, int value) {
		// TODO Auto-generated method stub
		client.incrBy(key, field, value);
		return Status.OK;
	}

	@Override
	public Status sum(String key1, String field, String key2) {
		// TODO Auto-generated method stub
		System.err.println("soma redis");
		try {
			client.sum(key1, field, key2).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status sumAll(String field) {
		// TODO Auto-generated method stub
		try {
			client.sumAll(field).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status multConst(String key, String field, int constant) {
		// TODO Auto-generated method stub
		try {
			client.multConst(key, field, constant).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status mult(String key1, String field, String key2) {
		// TODO Auto-generated method stub
		try {
			client.mult(key1, field, key2).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		return Status.OK;
	}

	@Override
	public Status multAll(String field) {
		// TODO Auto-generated method stub
		try {
			client.multAll(field).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status searchElement(String field, String value) {
		// TODO Auto-generated method stub
		try {
			client.searchElement(field, value).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status searchEntry(Map<String, String> set) {
		// TODO Auto-generated method stub
		try {
			client.searchEntry(set).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status orderEntrys(String field) {
		// TODO Auto-generated method stub
		try {
			client.orderEntrys(field).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status searchGreaterThan(String field, int value) {
		// TODO Auto-generated method stub
		try {
			client.searchGreaterThan(field, value).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status searchLesserThan(String field, int value) {
		// TODO Auto-generated method stub
		try {
			client.searchLesserThan(field, value).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}

	@Override
	public Status valuegreaterThan(String key1, String field, String key2) {
		// TODO Auto-generated method stub
		try {
			client.valuegreaterThan(key1, field, key2).get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK;
	}


}
