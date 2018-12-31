package com.nubeiot.core.http;

import java.util.ArrayList;
import java.util.Collection;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonParamParser {

    private static boolean isPretty(HttpServerRequest request) {
        return Boolean.valueOf(request.getParam("pretty"));
    }

    @SuppressWarnings("unchecked")
    public static <T> String prettify(T result, HttpServerRequest request) {
        boolean pretty = isPretty(request);
        if (result instanceof Collection) {
            final JsonArray jsonArray = new JsonArray(new ArrayList((Collection) result));
            return pretty ? jsonArray.encodePrettily() : jsonArray.encode();
        }
        final JsonObject jsonObject = JsonObject.mapFrom(result);
        return pretty ? jsonObject.encodePrettily() : jsonObject.encode();
    }

    public static String language(RoutingContext context) {
        String lang = context.request().getParam("lang");
        if (Strings.isBlank(lang)) {
            return "en";
        }
        return lang;
    }

}
