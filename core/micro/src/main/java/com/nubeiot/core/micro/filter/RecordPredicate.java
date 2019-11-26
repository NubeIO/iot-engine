package com.nubeiot.core.micro.filter;

import java.util.function.Function;
import java.util.function.Predicate;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.micro.ServiceGatewayIndex.Params;

import lombok.NonNull;

public interface RecordPredicate extends Function<JsonObject, Predicate<Record>> {

    static @NonNull Function<Record, Boolean> filter(@NonNull JsonObject filter, EventAction action) {
        JsonObject advanceFilter = new JsonObject().put(Params.TYPE, filter.remove(Params.TYPE))
                                                   .put(Params.STATUS, filter.remove(Params.STATUS))
                                                   .put(Params.SCOPE, filter.remove(Params.SCOPE))
                                                   .put(Params.IDENTIFIER, filter.remove(Params.IDENTIFIER))
                                                   .put(Params.BY, filter.remove(Params.BY));
        return record -> new CommonPredicate().apply(advanceFilter)
                                              .and(new IdentifierPredicate(action).apply(advanceFilter))
                                              .and(r -> r.match(filter))
                                              .test(record);
    }

    @Override
    @NonNull Predicate<Record> apply(@NonNull JsonObject filter);

}
