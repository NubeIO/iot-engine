package io.github.zero88.qwe.iot.connector.coordinator;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.scheduler.model.trigger.QWETriggerModel;
import io.vertx.core.json.JsonObject;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public final class WatcherOption implements JsonData {

    /**
     * Enable realtime mode that run when any event is occurred in a watcher object
     */
    private final boolean realtime;
    /**
     * Defines a real-time watcher is maintained in how long
     */
    @Default
    private final int lifetimeInSeconds = -1;
    /**
     * Enable trigger mode that run on a schedule or periodical, such as reading a sensor every five milliseconds
     */
    private final boolean trigger;

    /**
     * Defines trigger option if enable trigger mode
     */
    private final QWETriggerModel triggerOption;

    public static WatcherOption parse(@NonNull JsonObject watcher) {
        return JsonData.from(watcher, WatcherOption.class);
    }

}
