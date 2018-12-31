package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiConstants {

    public static final String ROOT_API_PATH = "/api";
    public static final String ROOT_WS_PATH = "/ws";
    public static final String ROOT_UPLOAD_PATH = "/ul";
    public static final String ROOT_DOWNLOAD_PATH = "/dl";
    public static final String PATH_WILDCARDS = "*";

    public static final String CONTENT_TYPE = "content-type";
    public static final String DEFAULT_CONTENT_TYPE = "application/json;charset=utf-8";

    public static final Set<HttpMethod> DEFAULT_CORS_HTTP_METHOD = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE,
                          HttpMethod.HEAD, HttpMethod.OPTIONS)));

}
