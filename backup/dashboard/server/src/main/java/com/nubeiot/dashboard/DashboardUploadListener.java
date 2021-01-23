package com.nubeiot.dashboard;

import static com.nubeiot.dashboard.ShareableMongoClient.SHARABLE_MONGO_CLIENT_DATA_KEY;
import static com.nubeiot.dashboard.constants.Collection.MEDIA_FILES;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.handler.UploadListener;

public class DashboardUploadListener extends UploadListener {

    public DashboardUploadListener(Vertx vertx, String sharedKey, List<EventAction> actions) {
        super(vertx, sharedKey, actions);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(JsonObject data) {
        JsonObject output = new JsonObject();
        ShareableMongoClient shareableMongoClient = SharedDataDelegate.getLocalDataValue(getVertx(), getSharedKey(),
                                                                                         SHARABLE_MONGO_CLIENT_DATA_KEY);
        List<String> keys = data.fieldNames()
                                .stream()
                                .filter(fieldName -> data.getJsonObject(fieldName).containsKey("fileName"))
                                .collect(Collectors.toList());

        return Observable.fromIterable(keys).flatMapSingle(fieldName -> {
            String name = data.getJsonObject(fieldName).getString("file");
            String title = data.getJsonObject(fieldName).getString("fileName");
            JsonObject mediaFile = new JsonObject().put("name", name).put("title", title);
            return shareableMongoClient.getMongoClient()
                                       .rxInsert(MEDIA_FILES, mediaFile)
                                       .map(id -> output.put(fieldName, id));
        }).toList().map(ignore -> output);
    }

}
