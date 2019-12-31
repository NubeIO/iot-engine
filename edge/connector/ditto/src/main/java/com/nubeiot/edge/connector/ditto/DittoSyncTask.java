package com.nubeiot.edge.connector.ditto;

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
import com.nubeiot.core.sql.workflow.task.EntityRuntimeContext;
import com.nubeiot.edge.connector.ditto.model.IDittoModel;

import lombok.NonNull;

public final class DittoSyncTask extends AbstractDittoTask<VertxPojo> {

    public DittoSyncTask(@NonNull DittoTaskContext definitionContext) {
        super(definitionContext);
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<VertxPojo> executionContext) {
        final EventAction action = executionContext.getOriginReqAction();
        return Single.just(
            action == EventAction.GET_LIST || action == EventAction.GET_ONE || action == EventAction.REMOVE ||
            !(definitionContext().entityHandler() instanceof EntitySyncHandler));
    }

    @Override
    public @NonNull Maybe<JsonObject> execute(@NonNull EntityRuntimeContext<VertxPojo> executionContext) {
        if (executionContext.isError()) {
            return doOnError(executionContext.getThrowable());
        }
        return transform(executionContext).flatMap(syncData -> doOnSuccess(executionContext.getMetadata(), syncData));
    }

    private @NonNull Maybe<IDittoModel<VertxPojo>> transform(@NonNull EntityRuntimeContext<VertxPojo> data) {
        ClassGraphCache<EntityMetadata, IDittoModel> cache = definitionContext().getSharedDataValue(SYNC_CONFIG_CACHE);
        return Maybe.just(Optional.ofNullable(IDittoModel.create(cache, data.getMetadata(), data.getData().toJson())))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
    }

    private Maybe<JsonObject> doOnSuccess(EntityMetadata metadata, IDittoModel<VertxPojo> syncData) {
        return doSyncOnSuccess(metadata, syncData.endpoint(thingId(definitionContext())), syncData.toJson(),
                               syncData.get());
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
