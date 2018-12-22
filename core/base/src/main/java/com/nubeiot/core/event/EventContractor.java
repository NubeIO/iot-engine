package com.nubeiot.core.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertx.core.json.JsonObject;
import lombok.NonNull;

/**
 * Marks class method to handle event type.
 *
 * @see EventAction
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventContractor {

    /**
     * @return the possible event types that a method can process
     */
    EventAction[] events();

    /**
     * @return Output type of method. Default: {@link JsonObject}
     */
    @NonNull Class<?> returnType() default JsonObject.class;

}
