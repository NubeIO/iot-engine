package com.nubeiot.core.sql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.Table;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Reflections.ReflectionField;

import lombok.NonNull;

public interface MetadataIndex {

    static List<EntityMetadata> find(@NonNull Class<? extends MetadataIndex> indexClass) {
        return ReflectionClass.stream(indexClass.getPackage().getName(), EntityMetadata.class,
                                      ReflectionClass.publicClass())
                              .flatMap(ReflectionField::streamConstants)
                              .collect(Collectors.toList());
    }

    List<EntityMetadata> index();

    default Optional<EntityMetadata> findByTable(@NonNull Table table) {
        return index().stream().filter(metadata -> table.equals(metadata.table())).findFirst();
    }

    default Optional<EntityMetadata> findByPojo(@NonNull Class<? extends VertxPojo> pojoClass) {
        return index().stream()
                      .filter(metadata -> ReflectionClass.assertDataType(metadata.modelClass(), pojoClass))
                      .findFirst();
    }

}
