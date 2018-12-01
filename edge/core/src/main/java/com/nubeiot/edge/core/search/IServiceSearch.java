package com.nubeiot.edge.core.search;

import com.nubeiot.core.dto.RequestData;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public interface IServiceSearch {

    Single<JsonObject> search(RequestData requestData);

}
