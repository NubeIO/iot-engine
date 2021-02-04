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

@Data
@Builder
@Jacksonized
public final class CoordinatorRegisterResult implements JsonData {

    private final String key;
    private final WatcherType watcherType;
    private final WatcherOption watcherOption;
    private final JsonObject watcherOutput;
    @Singular
    private final List<Subscriber> subscribers;

    public static @NonNull CoordinatorRegisterResult from(@NonNull CoordinatorInput input,
                                                          @NonNull WatcherType watcherType,
                                                          @NonNull JsonObject watcherOutput) {
        //noinspection unchecked
        return CoordinatorRegisterResult.builder()
                                        .watcherType(watcherType)
                                        .watcherOutput(watcherOutput)
                                        .watcherOption(input.getWatcherOption())
                                        .key(input.getSubject().key())
                                        .subscribers(input.getSubscribers())
                                        .build();
    }

    @JsonProperty("watcherType")
    public String watcherType() {
        return watcherType.type();
    }

}
