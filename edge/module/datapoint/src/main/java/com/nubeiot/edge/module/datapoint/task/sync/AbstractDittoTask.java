package com.nubeiot.edge.module.datapoint.task.sync;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.DataTransferObject;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.DataPointIndex;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class AbstractDittoTask<D extends VertxPojo> implements SyncTask<DittoTaskContext, D> {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DittoTaskContext taskContext;

    @Override
    public final DittoTaskContext definition() {
        return taskContext;
    }

    final String thingId(DittoTaskContext ctx) {
        return Strings.format("com.nubeio.{0}:{1}", ctx.getSharedDataValue(DataPointIndex.CUSTOMER_CODE),
                              UUID64.uuid64ToUuidStr(ctx.getSharedDataValue(DataPointIndex.EDGE_ID)));
    }

    final Maybe<JsonObject> doSyncOnSuccess(EntityMetadata metadata, String endpoint, JsonObject reqBody, D pojo) {
        EntitySyncHandler syncHandler = (EntitySyncHandler) definition().handler();
        final DittoTaskContext ctx = definition();
        final RequestData reqData = RequestData.builder().headers(ctx.createRequestHeader()).body(reqBody).build();
        return ctx.transporter()
                  .execute(endpoint, HttpMethod.PUT, reqData, false)
                  .map(DataTransferObject::body)
                  .flatMapMaybe(resp -> syncHandler.syncSuccess(metadata, pojo, resp, ctx.type()))
                  .onErrorResumeNext(
                      error -> (Maybe<JsonObject>) syncHandler.syncFailed(metadata, pojo, error, ctx.type()))
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
