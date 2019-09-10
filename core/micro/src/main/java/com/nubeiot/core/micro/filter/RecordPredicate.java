package com.nubeiot.core.micro.filter;

import java.util.function.Function;
import java.util.function.Predicate;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventAction;

import lombok.NonNull;

public interface RecordPredicate extends Function<JsonObject, Predicate<Record>> {

    String IDENTIFIER = "identifier";
    String TYPE = "type";
    String STATUS = "status";
    String SCOPE = "scope";
    String BY = "by";

    static @NonNull Function<Record, Boolean> filter(@NonNull JsonObject filter, EventAction action) {
        JsonObject advanceFilter = new JsonObject().put(TYPE, filter.remove(TYPE))
                                                   .put(STATUS, filter.remove(STATUS))
                                                   .put(SCOPE, filter.remove(SCOPE))
                                                   .put(IDENTIFIER, filter.remove(IDENTIFIER))
                                                   .put(BY, filter.remove(BY));
        return record -> new CommonPredicate().apply(advanceFilter)
                                              .and(new IdentifierPredicate(action).apply(advanceFilter))
                                              .and(r -> r.match(filter))
                                              .test(record);
    }

    @Override
    @NonNull Predicate<Record> apply(@NonNull JsonObject filter);

}
