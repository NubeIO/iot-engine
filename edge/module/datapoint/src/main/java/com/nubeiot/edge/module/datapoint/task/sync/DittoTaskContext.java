package com.nubeiot.edge.module.datapoint.task.sync;

import java.util.Objects;
import java.util.function.Function;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.task.EntityTaskContext;
import com.nubeiot.edge.module.datapoint.DataPointConfig.DataSyncConfig;

import lombok.NonNull;

final class DittoTaskContext extends AbstractEnumType implements EntityTaskContext<HttpClientDelegate> {

    static final String TYPE = "DITTO";
    @NonNull
    private final EntityHandler handler;
    @NonNull
    private final DataSyncConfig syncConfig;

    DittoTaskContext(EntityHandler handler, DataSyncConfig syncConfig) {
        super(TYPE);
        this.handler = handler;
        this.syncConfig = syncConfig;
    }

    @Override
    public final @NonNull EntityHandler handler() {
        return handler;
    }

    @Override
    public final boolean isConcurrent() {
        return true;
    }

    @Override
    public final DittoTaskContext registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

    @Override
    public final HttpClientDelegate transporter() {
        return HttpClientDelegate.create(vertx(), syncConfig.getClientConfig());
    }

    JsonObject createRequestHeader() {
        final JsonObject headers = new JsonObject().put(HttpHeaders.CONTENT_TYPE.toString(),
                                                        HttpUtils.JSON_UTF8_CONTENT_TYPE);
        if (Objects.nonNull(syncConfig.getCredential())) {
            headers.put(HttpHeaders.AUTHORIZATION.toString(), syncConfig.getCredential().toHeader());
        }
        return headers;
    }

}
