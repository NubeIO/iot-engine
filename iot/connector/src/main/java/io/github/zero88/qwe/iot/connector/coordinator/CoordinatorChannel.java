package io.github.zero88.qwe.iot.connector.coordinator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.iot.connector.Subject;
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

    private final String key;
    private final JsonObject subject;
    private final WatcherType watcherType;
    private final WatcherOption watcherOption;
    private final JsonObject watcherOutput;
    @Singular
    private final List<JsonObject> subscribers;

    public static @NonNull CoordinatorChannel from(@NonNull CoordinatorInput<? extends Subject> input,
                                                   @NonNull WatcherType watcherType, JsonObject watcherOutput) {
        return CoordinatorChannel.builder()
                                 .key(input.getSubject().key())
                                 .watcherType(watcherType)
                                 .watcherOutput(watcherOutput)
                                 .watcherOption(input.getWatcherOption())
                                 .subject(input.getSubject().toDetail())
                                 .subscribers(input.getSubscribers()
                                                   .stream()
                                                   .map(Subscriber::toJson)
                                                   .collect(Collectors.toList()))
                                 .build();
    }

    @JsonProperty("watcherType")
    public String watcherType() {
        return watcherType.type();
    }

    @JsonProperty("key")
    public String key() {
        return Optional.ofNullable(this.key)
                       .orElseGet(() -> Optional.ofNullable(subject).map(s -> s.getString("key")).orElse(null));
    }

}
