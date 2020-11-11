package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.Table;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero88.utils.Reflections.ReflectionClass;
import io.github.zero88.utils.Reflections.ReflectionField;

import lombok.NonNull;

/**
 * Defines Metadata index.
 *
 * @since 1.0.0
 */
public interface MetadataIndex {

    MetadataIndex BLANK = Collections::emptyList;

    /**
     * Find list of entity metadata by {@code reflection}.
     *
     * @param indexClass the index class
     * @return the list of entity metadata
     * @since 1.0.0
     */
    static List<EntityMetadata> find(@NonNull Class<? extends MetadataIndex> indexClass) {
        return ReflectionClass.stream(indexClass.getPackage().getName(), EntityMetadata.class,
                                      ReflectionClass.publicClass())
                              .flatMap(ReflectionField::streamConstants)
                              .collect(Collectors.toList());
    }

    /**
     * Defines Metadata Index list.
     *
     * @return the list
     * @since 1.0.0
     */
    List<EntityMetadata> index();

    /**
     * Find by table optional.
     *
     * @param table the table
     * @return the optional of entity metadata
     * @since 1.0.0
     */
    default Optional<EntityMetadata> findByTable(@NonNull Table table) {
        return index().stream().filter(metadata -> table.equals(metadata.table())).findFirst();
    }

    /**
     * Find by pojo optional.
     *
     * @param pojoClass the pojo class
     * @return the optional of entity metadata
     * @since 1.0.0
     */
    default Optional<EntityMetadata> findByPojo(@NonNull Class<? extends VertxPojo> pojoClass) {
        return index().stream()
                      .filter(metadata -> ReflectionClass.assertDataType(metadata.modelClass(), pojoClass))
                      .findFirst();
    }

}
