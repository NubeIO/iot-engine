package com.nubeiot.core.micro.filter;

import java.util.function.Predicate;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.micro.filter.ByPredicate.ByPredicateEnum;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class IdentifierPredicate implements RecordPredicate {

    private final EventAction action;

    @Override
    public @NonNull Predicate<Record> apply(@NonNull JsonObject filter) {
        String identifier = filter.getString(IDENTIFIER);
        if (Strings.isBlank(identifier)) {
            return r -> true;
        }
        return ByPredicateEnum.parse(action, filter.getString(BY)).by(identifier);
    }

}
