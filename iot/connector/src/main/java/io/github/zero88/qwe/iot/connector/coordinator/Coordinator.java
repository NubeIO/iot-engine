package io.github.zero88.qwe.iot.connector.coordinator;

import java.util.Arrays;
import java.util.Collection;

import io.github.zero88.qwe.dto.ErrorMessage;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventContractor.Param;
import io.github.zero88.qwe.event.EventPattern;
import io.github.zero88.qwe.event.Waybill;
import io.github.zero88.qwe.iot.connector.ConnectorService;
import io.github.zero88.qwe.iot.connector.Subject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import lombok.NonNull;

/**
 * Represents for an {@code coordinator service} that watches a particular {@code event} then notifying it to {@code
 * subscribers}
 */
public interface Coordinator<S extends Subject> extends ConnectorService {

    @EventContractor(action = "CREATE_OR_UPDATE", returnType = Single.class)
    Single<CoordinatorRegisterResult> register(@NonNull RequestData requestData);

    @EventContractor(action = "REMOVE", returnType = Single.class)
    Single<JsonObject> unregister(@NonNull RequestData requestData);

    @EventContractor(action = "GET_ONE", returnType = Single.class)
    Single<JsonObject> get(@NonNull RequestData requestData);

    @EventContractor(action = "MONITOR", returnType = boolean.class)
    boolean monitorThenNotify(@Param("data") JsonObject data, @Param("error") ErrorMessage error);

    @NonNull CoordinatorInput<S> parseInput(@NonNull RequestData requestData);

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE_OR_UPDATE, EventAction.REMOVE, EventAction.GET_ONE, EventAction.PATCH,
                             EventAction.MONITOR);
    }

    /**
     * Defines listen address then event will be
     *
     * @return waybill
     * @see #monitorThenNotify(JsonObject, ErrorMessage)
     */
    default Waybill listenAddress() {
        return Waybill.builder()
                      .address(this.getClass().getName())
                      .action(EventAction.MONITOR)
                      .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                      .build();
    }

    Waybill monitorAddress(JsonObject payload);

}
