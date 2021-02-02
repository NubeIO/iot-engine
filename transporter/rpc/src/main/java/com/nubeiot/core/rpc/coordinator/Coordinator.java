package com.nubeiot.core.rpc.coordinator;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventContractor;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import lombok.NonNull;

public interface Coordinator {

    @EventContractor(action = "CREATE", returnType = Single.class)
    Single<JsonObject> register(@NonNull RequestData requestData);

    default WatcherOption parseWatcher(@NonNull RequestData requestData) {
        return WatcherOption.parse(requestData.body().getJsonObject("watcher"));
    }

}
