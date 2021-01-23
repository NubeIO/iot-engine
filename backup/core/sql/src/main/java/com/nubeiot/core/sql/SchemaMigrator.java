package com.nubeiot.core.sql;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;

import lombok.NonNull;

/**
 * Represents for Schema migrator.
 *
 * @since 1.0.0
 */
public interface SchemaMigrator extends SchemaExecutor {

    SchemaMigrator NON_MIGRATOR = entityHandler -> Single.just(
        EventMessage.success(EventAction.MIGRATE, new JsonObject().put("records", "No migrate")));

    @Override
    Single<EventMessage> execute(@NonNull EntityHandler entityHandler);

}
