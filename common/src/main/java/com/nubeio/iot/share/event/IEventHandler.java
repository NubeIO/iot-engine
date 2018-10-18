package com.nubeio.iot.share.event;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public interface IEventHandler {

    Single<JsonObject> handle(EventType eventType, RequestData data);

}
