package com.nubeiot.core.sql;

import java.util.Map;

import org.jooq.Record;
import org.jooq.Table;

/**
 * {@inheritDoc} This keeps information about json fields that map with database column
 */
public interface JsonTable<R extends Record> extends Table<R> {

    Map<String, String> jsonFields();

}
