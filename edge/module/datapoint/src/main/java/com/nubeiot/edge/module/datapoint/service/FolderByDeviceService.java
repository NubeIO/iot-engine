package com.nubeiot.edge.module.datapoint.service;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;

import lombok.NonNull;

public final class FolderByDeviceService extends AbstractFolderExtensionService {

    public FolderByDeviceService(EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    public @NonNull EntityMetadata reference() {
        return DeviceMetadata.INSTANCE;
    }

    @Override
    public @NonNull Single<JsonObject> afterList(@NonNull JsonArray results) {
        //TODO temp way. Should improve by distinct in database layer
        return Observable.fromIterable(results)
                         .map(JsonObject.class::cast)
                         .distinct(r -> r.getString("id"))
                         .collect(JsonArray::new, JsonArray::add)
                         .map(arr -> new JsonObject().put(resourceMetadata().pluralKeyName(), arr));
    }

}
