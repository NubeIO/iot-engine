package com.nubeiot.edge.installer.search;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.installer.InstallerVerticle;

import lombok.NonNull;

public class RemoteServiceSearch implements IServiceSearch {

    private final InstallerVerticle verticle;

    public RemoteServiceSearch(@NonNull InstallerVerticle verticle) {
        this.verticle = verticle;
        
		//this.discovery = ServiceDiscovery.create( verticle.geRxtVert());
    }
	
	@Override
	public Single<JsonObject> search(RequestData requestData) {
	    return Single.just(new JsonObject());
	}


}

