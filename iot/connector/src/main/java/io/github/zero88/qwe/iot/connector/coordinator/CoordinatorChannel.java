package io.github.zero88.qwe.iot.connector.coordinator;

import java.util.List;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.iot.connector.subscriber.Subscriber;
import io.github.zero88.qwe.iot.connector.watcher.WatcherOption;
import io.github.zero88.qwe.iot.connector.watcher.WatcherType;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents for a coordinator channel
 */
@Data
@Builder
@Jacksonized
public final class CoordinatorChannel implements JsonData {

    private final JsonObject subject;
    private final WatcherType watcherType;
    private final WatcherOption watcherOption;
    private final JsonObject watcherOutput;
    @Singular
    private final List<Subscriber> subscribers;

    public static @NonNull CoordinatorChannel from(@NonNull CoordinatorInput input, @NonNull WatcherType watcherType,
                                                   @NonNull JsonObject watcherOutput) {
        //noinspection unchecked
        return CoordinatorChannel.builder()
                                 .watcherType(watcherType)
                                 .watcherOutput(watcherOutput)
                                 .watcherOption(input.getWatcherOption())
                                 .subject(input.getSubject().toDetail())
                                 .subscribers(input.getSubscribers())
                                 .build();
    }

    @JsonProperty("watcherType")
    public String watcherType() {
        return watcherType.type();
    }

}
