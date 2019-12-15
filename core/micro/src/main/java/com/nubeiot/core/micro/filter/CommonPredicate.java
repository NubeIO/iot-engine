package com.nubeiot.core.micro.filter;

import java.util.Objects;
import java.util.function.Predicate;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;

import com.nubeiot.core.micro.ServiceScope;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

final class CommonPredicate implements RecordPredicate {

    @Override
    public @NonNull Predicate<Record> apply(@NonNull JsonObject filter) {
        String type = filter.getString(TYPE);
        Status status = Objects.isNull(filter.getString(STATUS))
                        ? Status.UP
                        : Functions.getIfThrow(() -> Status.valueOf(filter.getString(STATUS).toUpperCase()))
                                   .orElse(Status.UP);
        ServiceScope scope = ServiceScope.parse(filter.getString(SCOPE));
        return byScope(scope, type).and(r -> r.getStatus().equals(status));
    }

    private Predicate<Record> byScope(@NonNull ServiceScope scope, final String type) {
        if (Strings.isNotBlank(type)) {
            return record -> record.getType().equals(type);
        }
        return scope.getPredicate();
    }

}
