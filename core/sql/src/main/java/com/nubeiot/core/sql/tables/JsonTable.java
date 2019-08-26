package com.nubeiot.core.sql.tables;

import java.util.Map;
import java.util.Map.Entry;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import lombok.NonNull;

/**
 * This keeps information about json fields that map with database column
 *
 * @see Table
 */
public interface JsonTable<R extends Record> extends Table<R> {

    Map<String, String> jsonFields();

    default Field getField(@NonNull String jsonName) {
        return field(jsonFields().getOrDefault(jsonName, jsonName));
    }

    default String getJsonField(@NonNull Field field) {
        return jsonFields().entrySet()
                           .stream()
                           .filter(entry -> entry.getValue().equals(field.getName()))
                           .findFirst()
                           .map(Entry::getKey)
                           .orElse(null);
    }

}
