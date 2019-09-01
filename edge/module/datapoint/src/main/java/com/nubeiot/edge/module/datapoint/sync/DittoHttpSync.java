package com.nubeiot.edge.module.datapoint.sync;

import java.util.Objects;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.auth.Credential;
import com.nubeiot.core.dto.DataTransferObject;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.sql.service.EntityPostService;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.model.ditto.IDittoModel;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex;

import lombok.NonNull;

public class DittoHttpSync extends AbstractEnumType
    implements EntityPostService<HttpClientDelegate, IDittoModel<? extends VertxPojo>> {

    public static final String TYPE = "DITTO";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final Vertx vertx;
    @NonNull
    private final JsonObject clientConfig;
    private final Credential credential;

    public DittoHttpSync(Vertx vertx, JsonObject clientConfig, Credential credential) {
        super(TYPE);
        this.vertx = vertx;
        this.clientConfig = clientConfig;
        this.credential = credential;
    }

    @Override
    public final HttpClientDelegate transporter() {
        return HttpClientDelegate.create(vertx, clientConfig);
    }

    @Override
    public IDittoModel<?> transform(@NonNull EntityService service, VertxPojo data) {
        return IDittoModel.create(service.context(), data.toJson());
    }

    @Override
    public void onSuccess(EntityService service, EventAction action, VertxPojo data) {
        if (action == EventAction.GET_LIST || action == EventAction.GET_ONE || !(service.entityHandler() instanceof EntitySyncHandler)) {
            return;
        }
        doSyncOnSuccess(service, action, transform(service, data));
    }

    @Override
    public void doSyncOnSuccess(@NonNull EntityService service, @NonNull EventAction action, IDittoModel<?> syncData) {
        final @NonNull EntitySyncHandler entityHandler = (EntitySyncHandler) service.entityHandler();
        final String thingId = Strings.format("com.nubeio.{0}:{1}",
                                              entityHandler.sharedData(DataPointIndex.CUSTOMER_CODE),
                                              UUID64.uuid64ToUuidStr(
                                                  entityHandler.sharedData(DataPointIndex.DEVICE_ID)));
        final JsonObject headers = new JsonObject().put(HttpHeaders.CONTENT_TYPE.toString(),
                                                        HttpUtils.DEFAULT_CONTENT_TYPE);
        if (Objects.nonNull(credential)) {
            headers.put(HttpHeaders.AUTHORIZATION.toString(), credential.computeHeader());
        }
        transporter().execute(syncData.endpoint(thingId), HttpMethod.PUT,
                              RequestData.builder().headers(headers).body(syncData.body()).build(), false)
                     .map(DataTransferObject::body)
                     .doOnSuccess(resp -> logger.debug("Sync success"))
                     .flatMapMaybe(resp -> entityHandler.syncSuccess(service.context(), syncData.get(), resp, type()))
                     .onErrorResumeNext(
                         error -> (Maybe<JsonObject>) entityHandler.syncFailed(service.context(), syncData.get(), error,
                                                                               type()))
                     .defaultIfEmpty(new JsonObject())
                     .subscribe(json -> {
                         if (json.isEmpty()) {
                             logger.debug("Skip updating synced status");
                         } else {
                             logger.info("Update synced status: {}", json.encode());
                         }
                     }, err -> logger.error("Failed when updating synced status of resource {}", err,
                                            service.context().table().getName()));
    }

    @Override
    public void onError(@NonNull EntityService service, EventAction action, Throwable throwable) {
        if (action == EventAction.GET_LIST || action == EventAction.GET_ONE) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.error("Not sync due to previous error", throwable);
        }
    }

}
