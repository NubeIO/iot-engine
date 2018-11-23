package com.nubeiot.core.event;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

public interface IEventHandler {

    Single<JsonObject> handle(@NonNull EventType eventType, @NonNull RequestData data) throws NubeException;

}
