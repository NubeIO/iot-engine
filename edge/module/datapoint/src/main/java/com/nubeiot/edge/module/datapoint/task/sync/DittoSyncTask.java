package com.nubeiot.edge.module.datapoint.task.sync;

import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.DesiredException;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.sql.workflow.task.EntityTaskData;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel;

import lombok.NonNull;

final class DittoSyncTask extends AbstractDittoTask<VertxPojo> {

    DittoSyncTask(@NonNull DittoTaskContext taskContext) {
        super(taskContext);
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityTaskData<VertxPojo> executionData) {
        final EventAction action = executionData.getOriginReqAction();
        return Single.just(
            action == EventAction.GET_LIST || action == EventAction.GET_ONE || action == EventAction.REMOVE ||
            !(definition().entityHandler() instanceof EntitySyncHandler));
    }

    @Override
    public @NonNull Maybe<JsonObject> execute(@NonNull EntityTaskData<VertxPojo> executionData) {
        if (executionData.isError()) {
            return doOnError(executionData.getThrowable());
        }
        return transform(executionData).flatMap(syncData -> doOnSuccess(executionData.getMetadata(), syncData));
    }

    private @NonNull Maybe<IDittoModel<VertxPojo>> transform(@NonNull EntityTaskData<VertxPojo> data) {
        ClassGraphCache<EntityMetadata, IDittoModel> cache = definition().getSharedDataValue(
            DataCacheInitializer.SYNC_CONFIG_CACHE);
        return Maybe.just(Optional.ofNullable(IDittoModel.create(cache, data.getMetadata(), data.getData().toJson())))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
    }

    private Maybe<JsonObject> doOnSuccess(EntityMetadata metadata, IDittoModel<VertxPojo> syncData) {
        return doSyncOnSuccess(metadata, syncData.endpoint(thingId(definition())), syncData.toJson(), syncData.get());
    }

    private Maybe<JsonObject> doOnError(Throwable throwable) {
        if (logger.isDebugEnabled()) {
            if (throwable instanceof DesiredException) {
                logger.debug("Not sync due to previous error", throwable);
            }
            logger.error("Not sync due to previous error", throwable);
        }
        return Maybe.empty();
    }

}
