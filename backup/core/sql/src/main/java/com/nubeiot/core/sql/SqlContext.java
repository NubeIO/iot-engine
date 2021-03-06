package com.nubeiot.core.sql;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.jooq.Configuration;

import io.github.zero88.exceptions.HiddenException;
import io.github.zero88.utils.Functions.Silencer;
import io.github.zero88.utils.Reflections.ReflectionClass;
import io.vertx.core.Vertx;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.exceptions.DatabaseException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SqlContext<T extends EntityHandler> extends UnitContext {

    @NonNull
    @Getter(value = AccessLevel.PACKAGE)
    private final Class<T> entityHandlerClass;

    @Getter
    private T entityHandler;

    T createHandler(@NonNull Configuration configuration, @NonNull Vertx vertx) {
        if (Objects.isNull(entityHandler)) {
            Map<Class, Object> map = new LinkedHashMap<>();
            map.put(Configuration.class, configuration);
            map.put(Vertx.class, vertx);
            entityHandler = ReflectionClass.createObject(entityHandlerClass, map, new CreationHandler<>()).get();
        }
        return entityHandler;
    }

    private static class CreationHandler<E extends EntityHandler> extends Silencer<E> {

        @Override
        public void accept(E obj, HiddenException e) {
            if (Objects.nonNull(e)) {
                throw new DatabaseException("Error when creating entity handler", e);
            }
            object = obj;
        }

    }

}
