package com.nubeiot.edge.core.loader;

import com.nubeiot.core.dto.RequestData;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public interface IServiceSearch {
	public Single<JsonObject> search(RequestData requestData);
}
