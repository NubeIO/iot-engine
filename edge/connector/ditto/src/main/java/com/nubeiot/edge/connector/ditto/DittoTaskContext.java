package com.nubeiot.edge.connector.ditto;

import java.util.Objects;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;

import com.nubeiot.auth.Credential;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.edge.module.datapoint.sync.SyncDefinitionContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class DittoTaskContext implements SyncDefinitionContext {

    public static final String TYPE = "DITTO";
    @NonNull
    private final EntityHandler entityHandler;
    @NonNull
    private final JsonObject transporterConfig;
    private final Credential credential;

    JsonObject createRequestHeader() {
        final JsonObject headers = new JsonObject().put(HttpHeaders.CONTENT_TYPE.toString(),
                                                        HttpUtils.JSON_UTF8_CONTENT_TYPE);
        if (Objects.nonNull(credential)) {
            headers.put(HttpHeaders.AUTHORIZATION.toString(), credential.toHeader());
        }
        return headers;
    }

    @Override
    public @NonNull String type() {
        return TYPE;
    }

}
