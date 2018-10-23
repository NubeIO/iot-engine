package com.nubeio.iot.share.event;

import com.nubeio.iot.share.dto.RequestData;
import com.nubeio.iot.share.exceptions.NubeException;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

public interface IEventHandler {

    Single<JsonObject> handle(@NonNull EventType eventType, @NonNull RequestData data) throws NubeException;

}
