package com.nubeiot.core.sql.pojos;

import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Represents for {@code DML pojo} after executing {@code DML} operation.
 *
 * @since 1.0.0
 */
@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public final class DMLPojo implements VertxPojo {

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

    @Override
    public VertxPojo fromJson(JsonObject json) {
        return this;
    }

    @Override
    public JsonObject toJson() {
        return Optional.ofNullable(dbEntity).orElse(request).toJson();
    }

}
