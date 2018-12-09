package com.nubeiot.edge.core.search;

import com.nubeiot.core.dto.RequestData;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class RemoteServiceSearch implements IServiceSearch {

    @Override
    public Single<JsonObject> search(RequestData requestData) {
        return Single.just(new JsonObject());
    }
    


}
