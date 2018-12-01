package com.nubeiot.edge.core.search;


import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.core.EdgeVerticle;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

public class RemoteServiceSearch implements IServiceSearch {
	
	private final EdgeVerticle verticle;
	
	public RemoteServiceSearch(@NonNull EdgeVerticle verticle) {
        this.verticle = verticle;
        
		//this.discovery = ServiceDiscovery.create( verticle.geRxtVert());
    }
	
	@Override
	public Single<JsonObject> search(RequestData requestData) {
	    return Single.just(new JsonObject());
	}


}

