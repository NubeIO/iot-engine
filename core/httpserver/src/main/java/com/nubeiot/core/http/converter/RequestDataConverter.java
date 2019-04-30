package com.nubeiot.core.http.converter;

import java.util.Objects;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.http.base.HttpUtils.HttpHeaderUtils;
import com.nubeiot.core.http.base.HttpUtils.HttpRequests;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

//TODO should convert only useful HEADER also
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestDataConverter {

    public static RequestData convert(RoutingContext context) {
        JsonObject mergeInput = JsonObject.mapFrom(context.pathParams());
        String body = context.getBodyAsString();
        if (Strings.isNotBlank(body)) {
            try {
                JsonObject bodyAsJson = context.getBodyAsJson();
                if (Objects.nonNull(bodyAsJson)) {
                    mergeInput.mergeIn(bodyAsJson, true);
                }
            } catch (DecodeException ex) {
                throw new HttpException("JSON payload is invalid", ex);
            }
        }
        final RequestData.Builder builder = RequestData.builder();
        Pagination pagination = HttpRequests.pagination(context.request());
        if (Objects.nonNull(pagination)) {
            builder.pagination(pagination);
        }
        return builder.headers(HttpHeaderUtils.serializeHeaders(context.request()))
                      .body(mergeInput)
                      .filter(HttpRequests.query(context.request()))
                      .build();
    }

    public static RequestData convert(ServerWebSocket context) {
        final RequestData.Builder builder = RequestData.builder();
        final String query = context.query();
        if (Strings.isBlank(query)) {
            return builder.build();
        }
        return builder.filter(JsonObject.mapFrom(HttpHeaderUtils.deserializeQuery(query))).build();
    }

}
