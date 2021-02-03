package io.github.zero88.qwe.iot.connector.coordinator;

import java.util.List;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.iot.connector.subscriber.Subscriber;
import io.github.zero88.qwe.iot.connector.watcher.WatcherOption;
import io.github.zero88.qwe.iot.connector.watcher.WatcherType;
import io.vertx.core.json.JsonObject;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public final class CoordinatorRegisterResult implements JsonData {

    private final String key;
    private final WatcherType watcherType;
    private final WatcherOption watcherOption;
    private final JsonObject watcherResult;
    private final List<Subscriber> subscribers;

}
