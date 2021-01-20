package com.nubeiot.core.rpc.subscriber;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.rpc.RpcClient;

import lombok.NonNull;

/**
 * Represents a register service that registers {@code Subscriber} into {@code Data Point repository} in startup phase
 * of the particular {@code protocol} application
 *
 * @param <T> Type of {@code DataProtocolSubscription}
 * @param <S> Type of {@code DataProtocolSubscriber}
 * @see RpcSubscriber
 * @since 1.0.0
 */
public interface RpcSubscription<P extends JsonData, T extends RpcSubscription, S extends RpcSubscriber<P>>
    extends RpcClient<P, T> {

    @Override
    default @NonNull ProtocolDispatcherMetadata context() {
        return ProtocolDispatcherMetadata.INSTANCE;
    }

    /**
     * Gets the registered subscribers.
     *
     * @return the set
     * @since 1.0.0
     */
    @NonNull Set<S> subscribers();

    /**
     * Gets logger.
     *
     * @return logger
     * @since 1.0.0
     */
    default Logger logger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Register subscriber.
     *
     * @param subscriber the subscriber
     * @return the result
     * @since 1.0.0
     */
    default Single<JsonObject> register(@NonNull S subscriber) {
        return invoke(subscriber, this::onRegister).doOnSuccess(r -> subscribers().add(subscriber))
                                                   .doOnSuccess(json -> logOnSuccess(subscriber, json, true));
    }

    /**
     * Unregister subscriber.
     *
     * @param subscriber the subscriber
     * @return the result
     * @since 1.0.0
     */
    default Single<JsonObject> unregister(@NonNull S subscriber) {
        return invoke(subscriber, this::onUnregister).doOnSuccess(r -> subscribers().remove(subscriber))
                                                     .doOnSuccess(json -> logOnSuccess(subscriber, json, false));
    }

    /**
     * Unregister all subscribers.
     *
     * @return the result
     * @since 1.0.0
     */
    default Single<JsonObject> unregisterAll() {
        return Observable.fromIterable(subscribers())
                         .flatMapSingle(this::unregister)
                         .filter(array -> !array.isEmpty())
                         .collect(JsonObject::new, JsonObject::mergeIn);
    }

    /**
     * Init protocol dispatcher on register.
     *
     * @param subscriber the subscriber
     * @param action     the action
     * @return the protocol dispatcher
     * @since 1.0.0
     */
    @NonNull
    default ProtocolDispatcher onRegister(@NonNull S subscriber, EventAction action) {
        return onCreateOrUpdate(subscriber, action, State.ENABLED);
    }

    /**
     * Init protocol dispatcher on unregister.
     *
     * @param subscriber the subscriber
     * @param action     the action
     * @return the protocol dispatcher
     * @since 1.0.0
     */
    @NonNull
    default ProtocolDispatcher onUnregister(@NonNull S subscriber, EventAction action) {
        return onCreateOrUpdate(subscriber, action, State.DISABLED);
    }

    /**
     * On create or update protocol dispatcher.
     *
     * @param subscriber the subscriber
     * @param action     the action
     * @param state      the state
     * @return the protocol dispatcher
     * @since 1.0.0
     */
    @NonNull
    default ProtocolDispatcher onCreateOrUpdate(@NonNull S subscriber, @NonNull EventAction action,
                                                @NonNull State state) {
        return new ProtocolDispatcher().setState(state)
                                       .setAddress(subscriber.address())
                                       .setProtocol(subscriber.protocol())
                                       .setAction(action)
                                       .setGlobal(subscriber.isGlobal())
                                       .setEntity(subscriber.context().singularKeyName());
    }

    /**
     * Invoke dispatcher service.
     *
     * @param subscriber the subscriber
     * @param converter  the converter
     * @return json result
     * @since 1.0.0
     */
    default Single<JsonObject> invoke(@NonNull S subscriber,
                                      @NonNull BiFunction<S, EventAction, ProtocolDispatcher> converter) {
        final EventAction action = EventAction.CREATE_OR_UPDATE;
        final Single<String> search = search(action);
        return search.flatMap(addr -> Observable.fromIterable(subscriber.getAvailableEvents()
                                                                        .stream()
                                                                        .map(a -> converter.apply(subscriber, a))
                                                                        .collect(Collectors.toList()))
                                                .flatMapSingle(p -> {
                                                    final RequestData req = RequestData.builder()
                                                                                       .body(JsonPojo.from(p).toJson())
                                                                                       .build();
                                                    return execute(addr, action, req);
                                                })
                                                .collect(JsonArray::new, JsonArray::add))
                     .map(array -> new JsonObject().put(subscriber.address(), array));
    }

    default void logOnSuccess(@NonNull S subscriber, JsonObject output, boolean isRegister) {
        final String msg = "{} {} subscriber event(s) of subscriber '{}'";
        logger().info(msg, isRegister ? "Enabled" : "Disabled", output.getJsonArray(subscriber.address()).size(),
                      subscriber.address());
        if (logger().isDebugEnabled()) {
            logger().debug("Detail subscriber record: {}", output);
        }
    }

}
