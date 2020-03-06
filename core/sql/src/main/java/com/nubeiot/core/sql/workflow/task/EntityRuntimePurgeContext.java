package com.nubeiot.core.sql.workflow.task;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public final class EntityRuntimePurgeContext<P extends VertxPojo> extends EntityRuntimeContext<P> {

    private final JsonObject result = new JsonObject();

    public EntityRuntimePurgeContext aggregate(@NonNull JsonObject result) {
        return this;
    }

}
