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
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.decorator.EntitySyncHandler;
import com.nubeiot.core.transport.ProxyService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

abstract class AbstractDittoHttpSync extends AbstractEnumType implements ProxyService<HttpClientDelegate> {

    public static final String TYPE = "DITTO";

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    @Getter(value = AccessLevel.PROTECTED)
    private final Vertx vertx;
    @NonNull
    private final JsonObject clientConfig;
    private final Credential credential;

    AbstractDittoHttpSync(Vertx vertx, JsonObject clientConfig, Credential credential) {
        super(TYPE);
        this.vertx = vertx;
        this.clientConfig = clientConfig;
        this.credential = credential;
    }

    @Override
    public final HttpClientDelegate transporter() {
        return HttpClientDelegate.create(vertx, clientConfig);
    }

    JsonObject createRequestHeader() {
        final JsonObject headers = new JsonObject().put(HttpHeaders.CONTENT_TYPE.toString(),
                                                        HttpUtils.JSON_UTF8_CONTENT_TYPE);
        if (Objects.nonNull(credential)) {
            headers.put(HttpHeaders.AUTHORIZATION.toString(), credential.toHeader());
        }
        return headers;
    }

    Maybe<JsonObject> doSyncOnSuccess(@NonNull EntitySyncHandler entityHandler, @NonNull EntityMetadata context,
                                      String endpoint, VertxPojo pojo, RequestData reqData) {
        return transporter().execute(endpoint, HttpMethod.PUT, reqData, false)
                            .map(DataTransferObject::body)
                            .doOnSuccess(resp -> logger.debug("Sync success"))
                            .flatMapMaybe(resp -> entityHandler.syncSuccess(context, pojo, resp, type()))
                            .onErrorResumeNext(
                                error -> (Maybe<JsonObject>) entityHandler.syncFailed(context, pojo, error, type()))
                            .defaultIfEmpty(new JsonObject())
                            .doOnSuccess(json -> {
                                if (json.isEmpty()) {
                                    logger.debug("Skip updating synced status");
                                } else {
                                    logger.info("Update synced status: {}", json.encode());
                                }
                            })
                            .doOnError(err -> logger.error("Failed when updating synced status of resource {}", err,
                                                           context.table().getName()));
    }

}
