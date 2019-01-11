package com.nubeiot.edge.connector.bonescript.utils;

import static com.nubeiot.core.http.ApiConstants.CONTENT_TYPE;
import static com.nubeiot.core.http.ApiConstants.DEFAULT_CONTENT_TYPE;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static void post(Vertx vertx, String uri, JsonObject body) {
        HttpClient client = vertx.getDelegate().createHttpClient();

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            logger.info("Posted the body content successfully");
        });

        request.setChunked(true);
        request.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        request.write(body.encode()).end();
    }

}
