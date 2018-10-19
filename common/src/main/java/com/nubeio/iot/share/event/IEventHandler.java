package com.nubeio.iot.share.event;

import com.nubeio.iot.share.exceptions.NubeException;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public interface IEventHandler {

    Single<JsonObject> handle(EventType eventType, RequestData data) throws NubeException;

}
