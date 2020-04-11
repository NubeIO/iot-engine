package com.nubeiot.core.micro.filter;

import java.util.function.Predicate;

import io.github.zero.utils.Strings;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.micro.ServiceGatewayIndex.Params;
import com.nubeiot.core.micro.filter.ByPredicate.ByPredicateEnum;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class IdentifierPredicate implements RecordPredicate {

    private final EventAction action;

    @Override
    public @NonNull Predicate<Record> apply(@NonNull JsonObject filter) {
        String identifier = filter.getString(Params.IDENTIFIER);
        if (Strings.isBlank(identifier)) {
            return r -> true;
        }
        return ByPredicateEnum.parse(action, filter.getString(Params.BY)).by(identifier);
    }

}
