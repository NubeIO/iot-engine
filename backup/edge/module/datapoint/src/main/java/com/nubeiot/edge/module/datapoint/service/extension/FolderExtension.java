package com.nubeiot.edge.module.datapoint.service.extension;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntityTransformer;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.FolderMetadata;
import com.nubeiot.edge.module.datapoint.model.pojos.FolderGroupComposite;

import lombok.NonNull;

public interface FolderExtension extends EntityTransformer {

    @Override
    default @NonNull EntityMetadata resourceMetadata() {
        return FolderMetadata.INSTANCE;
    }

    @Override
    default @NonNull Single<JsonObject> afterEach(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return convertToFolder(requestData, pojo);
    }

    @Override
    default @NonNull Single<JsonObject> afterGet(@NonNull VertxPojo pojo, @NonNull RequestData requestData) {
        return convertToFolder(requestData, pojo);
    }

    @Override
    default @NonNull Single<JsonObject> afterCreate(@NonNull Object key, @NonNull VertxPojo pojo,
                                                    @NonNull RequestData reqData) {
        return convertToFolder(reqData, pojo).map(json -> EntityTransformer.fullResponse(EventAction.CREATE, json));
    }

    @Override
    default @NonNull Single<JsonObject> afterUpdate(@NonNull Object key, @NonNull VertxPojo pojo,
                                                    @NonNull RequestData reqData) {
        return convertToFolder(reqData, pojo).map(json -> EntityTransformer.fullResponse(EventAction.UPDATE, json));
    }

    @Override
    default @NonNull Single<JsonObject> afterPatch(@NonNull Object key, @NonNull VertxPojo pojo,
                                                   @NonNull RequestData reqData) {
        return convertToFolder(reqData, pojo).map(json -> EntityTransformer.fullResponse(EventAction.PATCH, json));
    }

    default Single<JsonObject> convertToFolder(@NonNull RequestData reqData, @NonNull VertxPojo pojo) {
        final VertxPojo folder = ((FolderGroupComposite) pojo).getOther(FolderMetadata.INSTANCE.singularKeyName());
        return Single.just(JsonPojo.from(folder).toJson(ignoreFields(reqData)));
    }

}
