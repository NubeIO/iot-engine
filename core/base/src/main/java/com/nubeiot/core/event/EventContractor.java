package com.nubeiot.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertx.core.json.JsonObject;
import lombok.NonNull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventContractor {

    EventType[] events();

    @NonNull Class<?> returnType() default JsonObject.class;

}
