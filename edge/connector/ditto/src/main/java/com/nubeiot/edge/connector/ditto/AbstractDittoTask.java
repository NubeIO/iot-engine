package com.nubeiot.edge.connector.ditto;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.dto.DataTransferObject;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.connector.ditto.model.IDittoModel;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.sync.SyncTask;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
abstract class AbstractDittoTask<D extends VertxPojo> implements SyncTask<DittoTaskContext, D, HttpClientDelegate> {

    static final String SYNC_CONFIG_CACHE = "SYNC_CONFIG_CACHE";
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter
    @NonNull
    private final DittoTaskContext definitionContext;

    AbstractDittoTask(@NonNull DittoTaskContext definitionContext) {
        this.definitionContext = definitionContext;
        final @NonNull EntityHandler handler = this.definitionContext.entityHandler();
        handler.vertx()
               .executeBlocking(future -> future.complete(
                   new ClassGraphCache<EntityMetadata, IDittoModel>().register(IDittoModel::find)),
                                result -> handler.addSharedData(SYNC_CONFIG_CACHE, result.result()));
    }

    @Override
    public HttpClientDelegate transporter() {
        return HttpClientDelegate.create(definitionContext().vertx(), definitionContext().transporterConfig());
    }

    final String thingId(DittoTaskContext ctx) {
        return Strings.format("com.nubeio.{0}:{1}", ctx.getSharedDataValue(DataPointIndex.CUSTOMER_CODE),
                              UUID64.uuid64ToUuidStr(ctx.getSharedDataValue(DataPointIndex.EDGE_ID)));
    }

    final Maybe<JsonObject> doSyncOnSuccess(EntityMetadata metadata, String endpoint, JsonObject reqBody, D pojo) {
        final EntitySyncHandler syncHandler = (EntitySyncHandler) definitionContext().entityHandler();
        final DittoTaskContext ctx = definitionContext();
        final RequestData reqData = RequestData.builder().headers(ctx.createRequestHeader()).body(reqBody).build();
        return transporter().execute(endpoint, HttpMethod.PUT, reqData, false)
                            .map(DataTransferObject::body)
                            .flatMapMaybe(resp -> syncHandler.syncSuccess(metadata, pojo, resp, ctx.type()))
                            .onErrorResumeNext(
                                err -> (Maybe<JsonObject>) syncHandler.syncFailed(metadata, pojo, err, ctx.type()))
                            .defaultIfEmpty(new JsonObject())
                            .doOnSuccess(json -> {
                                if (json.isEmpty()) {
                                    logger.debug("Skip updating synced status");
                                } else {
                                    logger.info("Update synced status: {}", json.encode());
                                }
                            })
                            .doOnError(err -> logger.error("Failed when updating synced status of resource {}", err,
                                                           metadata.table().getName()))
                            .onErrorComplete();
    }

}
