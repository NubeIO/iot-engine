package com.nubeiot.edge.core.search;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;

public interface IServiceSearch {

    Single<JsonObject> search(RequestData requestData);

}
