package com.nubeiot.core.http.client;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;

import com.nubeiot.core.exceptions.HttpException;

/**
 * Temporary solution
 */
public class ClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClientUtils.class);

    /**
     * @deprecated
     */
    public static Single<Buffer> execute(HttpClient httpClient, String path, HttpMethod method, JsonObject headers,
                                         JsonObject payload, Handler<Void> closeHandler) {
        return Single.create(source -> {
            HttpClientRequest request = httpClient.request(method, path, response -> response.bodyHandler(body -> {
                logger.info("Response status {}", response.statusCode());
                if (response.statusCode() >= 400) {
                    source.onError(new HttpException(response.statusCode(), body.toString()));
                    logger.warn("Failed to execute: {}", response.toString());
                } else {
                    source.onSuccess(body);
                }
            })).endHandler(closeHandler).getDelegate();
            logger.info("Make HTTP request {} :: {} | <{}> | <{}>", request.method(), request.absoluteURI(), headers,
                        payload);
            //TODO why need it?
            request.setChunked(true);
            for (String header : headers.fieldNames()) {
                request.putHeader(header, headers.getValue(header).toString());
            }
            if (payload == null) {
                request.end();
            } else {
                request.write(payload.encode()).end();
            }
        });
    }

}
