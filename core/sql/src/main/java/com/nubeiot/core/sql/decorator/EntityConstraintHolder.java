package com.nubeiot.core.sql.decorator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Key;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;

import com.nubeiot.core.utils.Reflections.ReflectionField;

import lombok.NonNull;

/**
 * A {@code Holder} keeps information about {@code references/constraints} in database schema
 * <p>
 * In most case, it is decorator for EntityHandler
 *
 * @since 1.0.0
 */
public interface EntityConstraintHolder {

    /**
     * Key class class.
     *
     * @return the class
     * @since 1.0.0
     */
    @NonNull Class keyClass();

    /**
     * Find references to given {@code table}.
     *
     * @param table the table
     * @return table references to given table
     * @since 1.0.0
     */
    default List<Table> referencesTo(@NonNull Table table) {
        return streamReferenceKeysTo(table).map((Function<ForeignKey, Table>) Key::getTable)
                                           .collect(Collectors.toList());
    }

    /**
     * Find reference keys to given {@code table}.
     *
     * @param table the table
     * @return foreign keys to given table
     * @see ForeignKey
     * @since 1.0.0
     */
    default List<ForeignKey> referenceKeysTo(@NonNull Table table) {
        return streamReferenceKeysTo(table).collect(Collectors.toList());
    }

    /**
     * Stream reference table keys to given {@code table}.
     *
     * @param table the table
     * @return the stream of table and its foreign key
     * @since 1.0.0
     */
    default Stream<Entry<Table, Field>> streamReferenceTableKeysTo(@NonNull Table table) {
        return streamReferenceKeysTo(table).map(
            foreignKey -> new SimpleEntry<>(foreignKey.getTable(), (TableField) foreignKey.getFields().get(0)));
    }

    /**
     * Reference table keys to set.
     *
     * @param table the table
     * @return the set of table and its foreign key
     * @since 1.0.0
     */
    default Set<Entry<Table, Field>> referenceTableKeysTo(@NonNull Table table) {
        return streamReferenceTableKeysTo(table).collect(Collectors.toSet());
    }

    /**
     * Stream reference keys to given table.
     *
     * @param table the table
     * @return the stream of foreign key
     * @see ForeignKey
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default Stream<ForeignKey> streamReferenceKeysTo(@NonNull Table table) {
        return ReflectionField.streamConstants(keyClass(), UniqueKey.class)
                              .filter(key -> key.getTable().equals(table))
                              .map(key -> (List<ForeignKey>) key.getReferences())
                              .flatMap(Collection::stream);
    }

}
