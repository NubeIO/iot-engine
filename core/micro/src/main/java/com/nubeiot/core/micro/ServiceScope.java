package com.nubeiot.core.micro;

import java.util.function.Predicate;

import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;

import com.nubeiot.core.micro.type.EventMessageService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceScope {

    PUBLIC(ServiceScope::isPublic), INTERNAL(record -> !isPublic(record)), ALL(record -> true);

    @Getter
    private final Predicate<Record> predicate;

    public static ServiceScope parse(String scope) {
        if (INTERNAL.name().equalsIgnoreCase(scope)) {
            return INTERNAL;
        }
        if (ALL.name().equalsIgnoreCase(scope)) {
            return ALL;
        }
        return PUBLIC;
    }

    private static boolean isPublic(Record record) {
        return HttpEndpoint.TYPE.equalsIgnoreCase(record.getType()) ||
               EventMessageService.TYPE.equalsIgnoreCase(record.getType());
    }
}
