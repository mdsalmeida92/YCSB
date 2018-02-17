/**
 * Copyright (c) 2010 Yahoo! Inc., 2016-2017 YCSB contributors. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.yahoo.ycsb;

import java.util.Map;
import com.yahoo.ycsb.measurements.Measurements;
import org.apache.htrace.core.TraceScope;
import org.apache.htrace.core.Tracer;

import java.util.*;

/**
 * Wrapper around a "real" DB that measures latencies and counts return codes.
 * Also reports latency separately between OK and failed operations.
 */
public class DBWrapper extends DB {
	private final DB db;
	private final Measurements measurements;
	private final Tracer tracer;

	private boolean reportLatencyForEachError = false;
	private Set<String> latencyTrackedErrors = new HashSet<String>();

	private static final String REPORT_LATENCY_FOR_EACH_ERROR_PROPERTY = "reportlatencyforeacherror";
	private static final String REPORT_LATENCY_FOR_EACH_ERROR_PROPERTY_DEFAULT = "false";

	private static final String LATENCY_TRACKED_ERRORS_PROPERTY = "latencytrackederrors";

	private final String scopeStringCleanup;
	private final String scopeStringDelete;
	private final String scopeStringInit;
	private final String scopeStringInsert;
	private final String scopeStringRead;
	private final String scopeStringScan;
	private final String scopeStringUpdate;
	private final String scopeStringSum;
	private final  String scopeStringElementContainsSentence;
	private final  String scopeStringValuegreaterThan;
	private final  String scopeStringSearchLesserThan;
	private final  String scopeStringSearchGreaterThan;
	private final  String scopeStringOrderEntrys;
	private final  String scopeStringSearchEntry;
	private final  String scopeStringSearchElement;
	private final  String scopeStringMultAll;
	private final  String scopeStringMult;
	private final  String scopeStringMultConst;
	private final  String scopeStringSumAll;
	private final  String scopeStringIncr;
	private final  String scopeStringSearchEntryContainingSentence;

	public DBWrapper(final DB db, final Tracer tracer) {
		this.db = db;
		measurements = Measurements.getMeasurements();
		this.tracer = tracer;
		final String simple = db.getClass().getSimpleName();
		scopeStringCleanup = simple + "#cleanup";
		scopeStringDelete = simple + "#delete";
		scopeStringInit = simple + "#init";
		scopeStringInsert = simple + "#insert";
		scopeStringRead = simple + "#read";
		scopeStringScan = simple + "#scan";
		scopeStringUpdate = simple + "#update";
		scopeStringSum = simple + "#sum";
		scopeStringElementContainsSentence= simple + "#elementContainsSentence";
		scopeStringValuegreaterThan= simple + "#valuegreaterThan";
		scopeStringSearchLesserThan= simple + "#searchLesserThan";
		scopeStringSearchGreaterThan= simple + "#searchGreaterThan";
		scopeStringOrderEntrys= simple + "#orderEntrys";
		scopeStringSearchEntry= simple + "#searchEntry";
		scopeStringSearchElement= simple + "#searchElement";
		scopeStringMultAll= simple + "#multAll";
		scopeStringMult= simple + "#mult";
		scopeStringMultConst= simple + "#multConst";
		scopeStringSumAll= simple + "#sumAll";
		scopeStringIncr= simple + "#incr";
		scopeStringSearchEntryContainingSentence= simple + "#searchEntryContainingSentence";
	}

	/**
	 * Set the properties for this DB.
	 */
	public void setProperties(Properties p) {
		db.setProperties(p);
	}

	/**
	 * Get the set of properties for this DB.
	 */
	public Properties getProperties() {
		return db.getProperties();
	}

	/**
	 * Initialize any state for this DB.
	 * Called once per DB instance; there is one DB instance per client thread.
	 */
	public void init() throws DBException {
		try (final TraceScope span = tracer.newScope(scopeStringInit)) {
			db.init();

			this.reportLatencyForEachError = Boolean.parseBoolean(getProperties().
					getProperty(REPORT_LATENCY_FOR_EACH_ERROR_PROPERTY,
							REPORT_LATENCY_FOR_EACH_ERROR_PROPERTY_DEFAULT));

			if (!reportLatencyForEachError) {
				String latencyTrackedErrorsProperty = getProperties().getProperty(LATENCY_TRACKED_ERRORS_PROPERTY, null);
				if (latencyTrackedErrorsProperty != null) {
					this.latencyTrackedErrors = new HashSet<String>(Arrays.asList(
							latencyTrackedErrorsProperty.split(",")));
				}
			}

			System.err.println("DBWrapper: report latency for each error is " +
					this.reportLatencyForEachError + " and specific error codes to track" +
					" for latency are: " + this.latencyTrackedErrors.toString());
		}
	}

	/**
	 * Cleanup any state for this DB.
	 * Called once per DB instance; there is one DB instance per client thread.
	 */
	public void cleanup() throws DBException {
		try (final TraceScope span = tracer.newScope(scopeStringCleanup)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			db.cleanup();
			long en = System.nanoTime();
			measure("CLEANUP", Status.OK, ist, st, en);
		}
	}

	/**
	 * Read a record from the database. Each field/value pair from the result
	 * will be stored in a HashMap.
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to read.
	 * @param fields The list of fields to read, or null for all of them
	 * @param result A HashMap of field/value pairs for the result
	 * @return The result of the operation.
	 */
	public Status read(String table, String key, Set<String> fields,
			Map<String, ByteIterator> result) {
		try (final TraceScope span = tracer.newScope(scopeStringRead)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.read(table, key, fields, result);
			long en = System.nanoTime();
			measure("READ", res, ist, st, en);
			measurements.reportStatus("READ", res);
			return res;
		}
	}

	/**
	 * Perform a range scan for a set of records in the database.
	 * Each field/value pair from the result will be stored in a HashMap.
	 *
	 * @param table The name of the table
	 * @param startkey The record key of the first record to read.
	 * @param recordcount The number of records to read
	 * @param fields The list of fields to read, or null for all of them
	 * @param result A Vector of HashMaps, where each HashMap is a set field/value pairs for one record
	 * @return The result of the operation.
	 */
	public Status scan(String table, String startkey, int recordcount,
			Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
		try (final TraceScope span = tracer.newScope(scopeStringScan)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.scan(table, startkey, recordcount, fields, result);
			long en = System.nanoTime();
			measure("SCAN", res, ist, st, en);
			measurements.reportStatus("SCAN", res);
			return res;
		}
	}

	private void measure(String op, Status result, long intendedStartTimeNanos,
			long startTimeNanos, long endTimeNanos) {
		String measurementName = op;
		if (result == null || !result.isOk()) {
			if (this.reportLatencyForEachError ||
					this.latencyTrackedErrors.contains(result.getName())) {
				measurementName = op + "-" + result.getName();
			} else {
				measurementName = op + "-FAILED";
			}
		}
		measurements.measure(measurementName,
				(int) ((endTimeNanos - startTimeNanos) / 1000));
		measurements.measureIntended(measurementName,
				(int) ((endTimeNanos - intendedStartTimeNanos) / 1000));
	}

	/**
	 * Update a record in the database. Any field/value pairs in the specified values HashMap will be written into the
	 * record with the specified record key, overwriting any existing values with the same field name.
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to write.
	 * @param values A HashMap of field/value pairs to update in the record
	 * @return The result of the operation.
	 */
	public Status update(String table, String key,
			Map<String, ByteIterator> values) {
		try (final TraceScope span = tracer.newScope(scopeStringUpdate)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.update(table, key, values);
			long en = System.nanoTime();
			measure("UPDATE", res, ist, st, en);
			measurements.reportStatus("UPDATE", res);
			return res;
		}
	}

	/**
	 * Insert a record in the database. Any field/value pairs in the specified
	 * values HashMap will be written into the record with the specified
	 * record key.
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to insert.
	 * @param values A HashMap of field/value pairs to insert in the record
	 * @return The result of the operation.
	 */
	public Status insert(String table, String key,
			Map<String, ByteIterator> values) {
		try (final TraceScope span = tracer.newScope(scopeStringInsert)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.insert(table, key, values);
			long en = System.nanoTime();
			measure("INSERT", res, ist, st, en);
			measurements.reportStatus("INSERT", res);
			return res;
		}
	}

	/**
	 * Delete a record from the database.
	 *
	 * @param table The name of the table
	 * @param key The record key of the record to delete.
	 * @return The result of the operation.
	 */
	public Status delete(String table, String key) {
		try (final TraceScope span = tracer.newScope(scopeStringDelete)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.delete(table, key);
			long en = System.nanoTime();
			measure("DELETE", res, ist, st, en);
			measurements.reportStatus("DELETE", res);
			return res;
		}
	}

	@Override
	public Status elementContainsSentence(String key, String field, String word) {
		try (final TraceScope span = tracer.newScope(scopeStringElementContainsSentence)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.elementContainsSentence(key, field, word);
			long en = System.nanoTime();
			measure("ELEMENTCONTAINSSENTENCE", res, ist, st, en);
			measurements.reportStatus("ELEMENTCONTAINSSENTENCE", res);
			return res;
		}
	}

	@Override
	public Status searchEntryContainingSentence(String field, String word) {
		try (final TraceScope span = tracer.newScope(scopeStringSearchEntryContainingSentence)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.searchEntryContainingSentence(field, word);
			long en = System.nanoTime();
			measure("SEARCHENTRYCONTAININGSENTENCEP", res, ist, st, en);
			measurements.reportStatus("SEARCHENTRYCONTAININGSENTENCEP", res);
			return res;
		}
	}

	@Override
	public Status incr(String key, String field, int value) {
		try (final TraceScope span = tracer.newScope(scopeStringIncr)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.incr(key, field, value);
			long en = System.nanoTime();
			measure("INCR", res, ist, st, en);
			measurements.reportStatus("INCR", res);
			return res;
		}
	}

	@Override
	public Status sum(String key1, String field, String key2) {
		// TODO Auto-generated method stub
		try (final TraceScope span = tracer.newScope(scopeStringSum)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.sum(key1, field, key2);
			long en = System.nanoTime();
			measure("SUM", res, ist, st, en);
			measurements.reportStatus("SUM", res);
			return res;
		}

	}

	@Override
	public Status sumAll(String field) {
		try (final TraceScope span = tracer.newScope(scopeStringSumAll)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.sumAll(field);
			long en = System.nanoTime();
			measure("SUMALL", res, ist, st, en);
			measurements.reportStatus("SUMALL", res);
			return res;
		}
	}

	@Override
	public Status multConst(String key, String field, int constant) {
		try (final TraceScope span = tracer.newScope(scopeStringMultConst)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.multConst(key, field, constant);
			long en = System.nanoTime();
			measure("MULTCONST", res, ist, st, en);
			measurements.reportStatus("MULTCONST", res);
			return res;
		}
	}

	@Override
	public Status mult(String key1, String field, String key2) {
		try (final TraceScope span = tracer.newScope(scopeStringMult)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.mult(key1, field, key2);
			long en = System.nanoTime();
			measure("MULT", res, ist, st, en);
			measurements.reportStatus("MULT", res);
			return res;
		}
	}

	@Override
	public Status multAll(String field) {
		try (final TraceScope span = tracer.newScope(scopeStringMultAll)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.multAll(field);
			long en = System.nanoTime();
			measure("MULTALL", res, ist, st, en);
			measurements.reportStatus("MULTALL", res);
			return res;
		}
	}

	@Override
	public Status searchElement(String field, String value) {
		try (final TraceScope span = tracer.newScope(scopeStringSearchElement)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.searchElement(field, value);
			long en = System.nanoTime();
			measure("SEARCHELEMENT", res, ist, st, en);
			measurements.reportStatus("SEARCHELEMENT", res);
			return res;
		}
	}

	@Override
	public Status searchEntry(Map<String, String> set) {
		try (final TraceScope span = tracer.newScope(scopeStringSearchEntry)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.searchEntry(set);
			long en = System.nanoTime();
			measure("SEARCHENTRY", res, ist, st, en);
			measurements.reportStatus("SEARCHENTRY", res);
			return res;
		}
	}

	@Override
	public Status orderEntrys(String field) {
		try (final TraceScope span = tracer.newScope(scopeStringOrderEntrys)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.orderEntrys(field);
			long en = System.nanoTime();
			measure("ORDERENTRYS", res, ist, st, en);
			measurements.reportStatus("ORDERENTRYS", res);
			return res;
		}
	}

	@Override
	public Status searchGreaterThan(String field, int value) {
		try (final TraceScope span = tracer.newScope(scopeStringSearchGreaterThan)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.searchGreaterThan(field, value);
			long en = System.nanoTime();
			measure("SEARCHGREATERTHAN", res, ist, st, en);
			measurements.reportStatus("SEARCHGREATERTHAN", res);
			return res;
		}
	}

	@Override
	public Status searchLesserThan(String field, int value) {
		try (final TraceScope span = tracer.newScope(scopeStringSearchLesserThan)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.searchLesserThan(field, value);
			long en = System.nanoTime();
			measure("SEARCHLESSERTHAN", res, ist, st, en);
			measurements.reportStatus("SEARCHLESSERTHAN", res);
			return res;
		}
	}

	@Override
	public Status valuegreaterThan(String key1, String field, String key2) {
		try (final TraceScope span = tracer.newScope(scopeStringValuegreaterThan)) {
			long ist = measurements.getIntendedtartTimeNs();
			long st = System.nanoTime();
			Status res = db.valuegreaterThan(key1, field, key2);
			long en = System.nanoTime();
			measure("VALUEGREATERTHAN", res, ist, st, en);
			measurements.reportStatus("VALUEGREATERTHAN", res);
			return res;
		}
	}
}
