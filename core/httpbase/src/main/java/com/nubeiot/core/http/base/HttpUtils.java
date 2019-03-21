package com.nubeiot.core.http.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils {

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

    public static final class HttpRequests {

        public static String language(@NonNull HttpServerRequest request) {
            String lang = request.getParam("lang");
            if (Strings.isBlank(lang)) {
                return "en";
            }
            return lang;
        }

        public static Pagination pagination(@NonNull HttpServerRequest request) {
            if (request.method() == HttpMethod.GET) {
                return Pagination.builder()
                                 .page(request.getParam("page"))
                                 .perPage(request.getParam("per_page"))
                                 .build();
            }
            return null;
        }

        public static JsonObject query(@NonNull HttpServerRequest request) {
            String query = request.query();
            return Strings.isBlank(query) ? new JsonObject() : JsonObject.mapFrom(HttpUtils.deserializeQuery(query));
        }

        public static JsonObject serializeHeaders(@NonNull HttpServerRequest request) {
            return serializeHeaders(request.headers());
        }

        public static JsonObject serializeHeaders(@NonNull MultiMap multiMap) {
            JsonObject headers = new JsonObject();
            multiMap.names().forEach(name -> {
                final List<String> byNames = multiMap.getAll(name);
                if (byNames.isEmpty()) {
                    return;
                }
                headers.put(name, byNames.size() > 1 ? byNames : byNames.get(0));
            });
            return headers;
        }

        public static MultiMap deserializeHeaders(@NonNull JsonObject headers) {
            final MultiMap map = MultiMap.caseInsensitiveMultiMap();
            if (headers.isEmpty()) {
                return map;
            }
            headers.stream().filter(entry -> Objects.nonNull(entry.getValue())).forEach(entry -> {
                if (entry.getValue() instanceof JsonArray) {
                    List<String> values = ((JsonArray) entry.getValue()).stream()
                                                                        .map(Object::toString)
                                                                        .collect(Collectors.toList());
                    map.add(entry.getKey(), values);
                } else {
                    map.add(entry.getKey(), entry.getValue().toString());
                }
            });
            return map;
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HttpMethods {

        private static final List<HttpMethod> SINGULAR_HTTP_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.DELETE,
                                                                                    HttpMethod.PUT, HttpMethod.PATCH);

        public static boolean isSingular(HttpMethod method) {
            return SINGULAR_HTTP_METHODS.contains(method);
        }

    }

}
