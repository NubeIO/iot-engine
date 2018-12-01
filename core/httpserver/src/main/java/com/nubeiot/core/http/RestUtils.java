package com.nubeiot.core.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestUtils {

    private static final String EQUAL = "=";
    private static final String SEPARATE = "&";

    public static Map<String, Object> deserializeQuery(String query) {
        Map<String, Object> map = new HashMap<>();
        for (String property : query.split("\\" + SEPARATE)) {
            String[] keyValues = property.split("\\" + EQUAL);
            if (keyValues.length != 2) {
                throw new InvalidUrlException("Property doesn't conform the syntax: `key`" + EQUAL + "`value`");
            }
            map.put(Urls.decode(keyValues[0]), Urls.decode(keyValues[1]));
        }
        return map;
    }

    public static RequestData convertToRequestData(RoutingContext context) {
        JsonObject mergeInput = JsonObject.mapFrom(context.pathParams());
        String body = context.getBodyAsString();
        if (Strings.isNotBlank(body)) {
            final JsonObject bodyAsJson = context.getBodyAsJson();
            if (Objects.nonNull(bodyAsJson)) {
                mergeInput.mergeIn(bodyAsJson, true);
            }
        }
        Pagination pagination = Pagination.builder()
                                          .page(context.queryParams().get("page"))
                                          .perPage(context.queryParams().get("per_page"))
                                          .build();
        final String query = context.request().query();
        if (Strings.isBlank(query)) {
            return RequestData.builder().pagination(pagination).body(mergeInput).build();
        }
        return RequestData.builder()
                          .pagination(pagination)
                          .body(mergeInput)
                          .filter(JsonObject.mapFrom(deserializeQuery(query)))
                          .build();
    }

}
