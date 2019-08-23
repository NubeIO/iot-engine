package com.nubeiot.core.sql.tables;

import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

/**
 * This keeps information about json fields that map with database column
 *
 * @see Table
 */
public interface JsonTable<R extends Record> extends Table<R> {

    Map<String, String> jsonFields();

    default Field getField(String jsonName) {
        return field(jsonFields().getOrDefault(jsonName, jsonName));
    }

}
