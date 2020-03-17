package com.nubeiot.edge.connector.ditto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    //TODO need to exclude REMOVE
    public static final Set<EventAction> UNSUPPORTED_SYNC_ACTION = new HashSet<>(
        Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE, EventAction.REMOVE));

    public DittoSyncTask(@NonNull DittoTaskContext definitionContext) {
        super(definitionContext);
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<VertxPojo> runtimeContext) {
        final EventAction action = runtimeContext.getOriginReqAction();
        return Single.just(definitionContext().entityHandler() instanceof EntitySyncHandler &&
                           !UNSUPPORTED_SYNC_ACTION.contains(action));
    }

    @Override
    public @NonNull Maybe<JsonObject> execute(@NonNull EntityRuntimeContext<VertxPojo> runtimeContext) {
        if (runtimeContext.isError()) {
            return doOnError(runtimeContext.getThrowable(), runtimeContext.getOriginReqAction(),
                             runtimeContext.getMetadata());
        }
        return transform(runtimeContext).flatMap(syncData -> doOnSuccess(runtimeContext.getMetadata(), syncData));
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

    private Maybe<JsonObject> doOnError(@NonNull Throwable throwable, @NonNull EventAction action,
                                        @NonNull EntityMetadata metadata) {
        if (logger.isDebugEnabled()) {
            if (throwable instanceof DesiredException) {
                logger.debug("Not sync {}::{} due to previous error", throwable, metadata.table().getName(), action);
            }
            logger.warn("Not sync {}::{} due to previous error", throwable, metadata.table().getName(), action);
        }
        return Maybe.empty();
    }

}
