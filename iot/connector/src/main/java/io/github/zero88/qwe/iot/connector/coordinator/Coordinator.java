package io.github.zero88.qwe.iot.connector.coordinator;

import java.util.Arrays;
import java.util.Collection;

import io.github.zero88.qwe.dto.ErrorMessage;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventContractor.Param;
import io.github.zero88.qwe.event.Waybill;
import io.github.zero88.qwe.iot.connector.ConnectorService;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import lombok.NonNull;

/**
 * Represents for an {@code coordinator service} that watches a particular {@code event} then notifying it to {@code
 * subscribers}
 */
public interface Coordinator extends ConnectorService {

    default String function() {
        return "coordinator";
    }

    @EventContractor(action = "CREATE", returnType = Single.class)
    Single<JsonObject> register(@NonNull RequestData requestData);

    @EventContractor(action = "REMOVE", returnType = Single.class)
    Single<JsonObject> unregister(@NonNull RequestData requestData);

    @EventContractor(action = "MONITOR", returnType = boolean.class)
    boolean monitorThenNotify(@Param("data") JsonObject data, @Param("error") ErrorMessage error);

    Observable<Subscriber> subscribers();

    default WatcherOption parseWatcher(@NonNull RequestData requestData) {
        return WatcherOption.parse(requestData.body().getJsonObject("watcher"));
    }

    default Subscriber parseSubscriber(@NonNull RequestData requestData) {
        return Subscriber.parse(requestData.body().getJsonObject("subscriber"));
    }

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.REMOVE, EventAction.MONITOR);
    }

    /**
     * Defines listen address then event will be
     *
     * @return waybill
     * @see #monitorThenNotify(JsonObject, ErrorMessage)
     */
    default Waybill listenAddress() {
        return Waybill.builder().address(this.getClass().getName()).action(EventAction.MONITOR).build();
    }

    Waybill monitorAddress(JsonObject payload);

}
