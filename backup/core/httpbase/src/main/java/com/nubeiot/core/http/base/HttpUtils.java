package com.nubeiot.core.http.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero88.exceptions.InvalidUrlException;
import io.github.zero88.utils.Strings;
import io.github.zero88.utils.Urls;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.Pagination;
import com.nubeiot.core.dto.RequestFilter.Filters;
import com.nubeiot.core.dto.Sort;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpUtils {

    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String JSON_UTF8_CONTENT_TYPE = "application/json;charset=utf-8";
    public static final String NONE_CONTENT_TYPE = "no-content-type";
    public static final Set<HttpMethod> DEFAULT_CORS_HTTP_METHOD = Collections.unmodifiableSet(new HashSet<>(
        Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE,
                      HttpMethod.HEAD, HttpMethod.OPTIONS)));

    private static boolean isPretty(HttpServerRequest request) {
        return Boolean.parseBoolean(request.getParam(Filters.PRETTY));
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

    public static final class HttpHeaderUtils {

        public static JsonObject serializeHeaders(@NonNull HttpServerRequest request) {
            return serializeHeaders(request.headers());
        }

        public static JsonObject serializeHeaders(@NonNull HttpServerResponse request) {
            return serializeHeaders(request.headers());
        }

        public static JsonObject serializeHeaders(@NonNull HttpClientRequest request) {
            return serializeHeaders(request.headers());
        }

        public static JsonObject serializeHeaders(@NonNull HttpClientResponse response) {
            return serializeHeaders(response.headers());
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


    public static final class HttpRequests {

        private static final String EQUAL = "=";
        private static final String SEPARATE = "&";

        public static String language(@NonNull HttpServerRequest request) {
            String lang = request.getParam(Filters.LANG);
            if (Strings.isBlank(lang)) {
                return "en";
            }
            return lang;
        }

        public static Pagination pagination(@NonNull HttpServerRequest request) {
            if (request.method() == HttpMethod.GET) {
                return Pagination.builder()
                                 .page(request.getParam(Filters.PAGE))
                                 .perPage(request.getParam(Filters.PER_PAGE))
                                 .build();
            }
            return null;
        }

        public static Sort sort(@NonNull HttpServerRequest request) {
            if (request.method() == HttpMethod.GET) {
                return Sort.from(Urls.decode(Optional.ofNullable(request.getParam(Filters.SORT)).orElse("")));
            }
            return null;
        }

        public static JsonObject query(@NonNull HttpServerRequest request) {
            final Map<String, Object> map = request.params()
                                                   .entries()
                                                   .stream()
                                                   .collect(Collectors.toMap(Entry::getKey,
                                                                             entry -> (Object) entry.getValue(),
                                                                             (o, o2) -> {
                                                                                 if (o instanceof String) {
                                                                                     return new JsonArray().add(o)
                                                                                                           .add(o2);
                                                                                 }
                                                                                 return ((JsonArray) o).add(o2);
                                                                             }));
            Filters.BOOLEAN_PARAMS.forEach(s -> map.computeIfPresent(s, (s1, o) -> true));
            return JsonObject.mapFrom(map);
        }

        public static JsonObject deserializeQuery(String query) {
            Map<String, Object> map = new HashMap<>();
            for (String property : query.split("\\" + SEPARATE)) {
                String[] keyValues = property.split("\\" + EQUAL);
                String propKey = Urls.decode(keyValues[0]);
                if (Filters.AUDIT.equals(propKey) || Filters.PRETTY.equals(propKey)) {
                    map.put(propKey, true);
                    continue;
                }
                if (keyValues.length != 2) {
                    throw new InvalidUrlException("Property doesn't conform the syntax: `key`" + EQUAL + "`value`");
                }
                map.put(propKey, Urls.decode(keyValues[1]));
            }
            return JsonObject.mapFrom(map);
        }

        public static String serializeQuery(JsonObject filter) {
            return Objects.isNull(filter)
                   ? null
                   : filter.fieldNames()
                           .stream()
                           .map(name -> name.concat(EQUAL).concat(Urls.encode(Strings.toString(filter.getValue(name)))))
                           .collect(Collectors.joining(SEPARATE));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HttpMethods {

        private static final List<HttpMethod> SINGULAR_HTTP_METHODS = Arrays.asList(HttpMethod.GET, HttpMethod.DELETE,
                                                                                    HttpMethod.PUT, HttpMethod.PATCH);

        public static boolean isSingular(HttpMethod method) {
            return SINGULAR_HTTP_METHODS.contains(method);
        }

        public static boolean hasBody(HttpMethod method) {
            return HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.PATCH.equals(method) ||
                   HttpMethod.TRACE.equals(method);
        }

    }

}
