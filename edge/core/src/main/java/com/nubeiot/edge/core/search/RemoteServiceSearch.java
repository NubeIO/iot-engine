package com.nubeiot.edge.core.search;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.core.EdgeVerticle;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import lombok.NonNull;

public class RemoteServiceSearch implements IServiceSearch {

    private final EdgeVerticle verticle;

    public RemoteServiceSearch(@NonNull EdgeVerticle verticle) {
        this.verticle = verticle;
    }

    @Override
    public Single<JsonObject> search(RequestData requestData) {

        return Single.just(new JsonObject());
    }
    
    private JsonObject validateFilter (JsonObject filter) {
        
        return new JsonObject();
    }

    public static void main(String args[]) {
	    Vertx vertx = Vertx.vertx();
	    JsonArray result = new JsonArray();
        WebClient client = WebClient.create(vertx);
        try {
            
            JsonArray jsonArray = client
                .get(8081, "localhost", "/service/rest/v1/search").addQueryParam("repository", "maven-central")
                    .rxSend().flatMap(response -> {
                        JsonArray jsonArray2 = response.bodyAsJsonObject().getJsonArray("items");
                        System.out.println(jsonArray2);
                        return Single.just(jsonArray2);
                    }).toFuture().get();
            result.addAll(jsonArray);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println(result);
	}

}
