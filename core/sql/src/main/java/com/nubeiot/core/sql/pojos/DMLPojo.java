package com.nubeiot.core.sql.pojos;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Represents for {@code DML pojo}.
 *
 * @since 1.0.0
 */
@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public final class DMLPojo {

    /**
     * Request entity
     */
    private final VertxPojo request;
    /**
     * Entity primary key
     */
    private final Object primaryKey;
    /**
     * Database entity
     */
    private final VertxPojo dbEntity;

    public static DMLPojo clone(@NonNull DMLPojo dmlPojo, @NonNull VertxPojo dbEntity) {
        return DMLPojo.builder().request(dmlPojo.request()).primaryKey(dmlPojo.primaryKey()).dbEntity(dbEntity).build();
    }

}
