package com.nubeiot.core.micro.filter;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventAction;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface ByPredicate {

    Predicate<Record> by(@NonNull String identifier);

    @RequiredArgsConstructor
    enum ByPredicateEnum implements ByPredicate {
        REGISTRATION("REGISTRATION", null, id -> record -> id.equalsIgnoreCase(record.getRegistration())),
        GROUP_MANY("GROUP", EventAction.GET_LIST, id -> r -> r.getName().toLowerCase().startsWith(id.toLowerCase())),
        GROUP_ONE("GROUP", EventAction.GET_ONE, id -> record -> {
            final int idx = record.getName().lastIndexOf(".");
            return idx != -1 && record.getName().substring(0, idx).equalsIgnoreCase(id.toLowerCase());
        }),
        NAME_MANY("NAME", EventAction.GET_LIST, id -> r -> r.getName().toLowerCase().contains(id.toLowerCase())),
        NAME_ONE("NAME", EventAction.GET_ONE, id -> record -> record.getName().equalsIgnoreCase(id.toLowerCase())),
        PATH("PATH", null, id -> record -> ByPathPredicate.predicate(record, id));

        private final String type;
        private final EventAction action;
        private final Function<String, Predicate<Record>> func;

        static ByPredicateEnum parse(@NonNull EventAction action, String by) {
            return Stream.of(ByPredicateEnum.values())
                         .filter(anEnum -> anEnum.type.equalsIgnoreCase(by) &&
                                           (action.equals(anEnum.action) || anEnum.action == null))
                         .findFirst()
                         .orElse(ByPredicateEnum.REGISTRATION);
        }

        @Override
        public Predicate<Record> by(String identifier) {
            return func.apply(identifier);
        }
    }

}
