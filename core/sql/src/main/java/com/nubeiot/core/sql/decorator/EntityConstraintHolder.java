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

public interface EntityConstraintHolder {

    @NonNull Class keyClass();

    default List<Table> referencesTo(@NonNull Table table) {
        return streamReferenceKeysTo(table).map((Function<ForeignKey, Table>) Key::getTable)
                                           .collect(Collectors.toList());
    }

    default List<ForeignKey> referenceKeysTo(@NonNull Table table) {
        return streamReferenceKeysTo(table).collect(Collectors.toList());
    }

    default Stream<Entry<Table, Field>> streamReferenceTableKeysTo(@NonNull Table table) {
        return streamReferenceKeysTo(table).map(
            foreignKey -> new SimpleEntry<>(foreignKey.getTable(), (TableField) foreignKey.getFields().get(0)));
    }

    default Set<Entry<Table, Field>> referenceTableKeysTo(@NonNull Table table) {
        return streamReferenceTableKeysTo(table).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    default Stream<ForeignKey> streamReferenceKeysTo(@NonNull Table table) {
        return ReflectionField.streamConstants(keyClass(), UniqueKey.class)
                              .filter(key -> key.getTable().equals(table))
                              .map(key -> (List<ForeignKey>) key.getReferences())
                              .flatMap(Collection::stream);
    }

}
