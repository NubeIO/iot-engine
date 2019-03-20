package com.nubeiot.core.http.client;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

/**
 * Temporary solution
 */
public class ClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClientUtils.class);

    public static Single<ResponseData> execute(HttpClient httpClient, String path, HttpMethod method,
                                               RequestData requestData, Handler<Void> closeHandler) {
        return Single.create(source -> {
            HttpClientRequest request = httpClient.request(method, path, response -> response.bodyHandler(body -> {
                logger.info("Response data {}", body.toString());
                if (response.statusCode() >= 400) {
                    ErrorCode errorCode = HttpStatusMapping.error(method,
                                                                  HttpResponseStatus.valueOf(response.statusCode()));
                    source.onError(new NubeException(errorCode, body.toString()));
                    logger.warn("Failed to execute: {} | {} | {}", method, path, response.statusCode());
                } else {
                    source.onSuccess(new ResponseData().setHeaders(JsonObject.mapFrom(response.headers()))
                                                       .setBody(body.toJsonObject()));
                }
            })).endHandler(closeHandler);
            logger.info("Make HTTP request {} :: {} | <{}> | <{}>", request.method(), request.absoluteURI(),
                        requestData.toJson());
            //TODO why need it?
            request.setChunked(true);
            for (String header : requestData.headers().fieldNames()) {
                request.putHeader(header, requestData.headers().getValue(header).toString());
            }
            if (requestData.body() == null) {
                request.end();
            } else {
                request.write(requestData.body().encode()).end();
            }
        });
    }

    /**
     * @deprecated
     */
    public static Single<Buffer> execute(HttpClient httpClient, String path, HttpMethod method, JsonObject headers,
                                         JsonObject payload, Handler<Void> closeHandler) {
        return Single.create(source -> {
            HttpClientRequest request = httpClient.request(method, path, response -> response.bodyHandler(body -> {
                logger.debug("Response status {}", response.statusCode());
                if (response.statusCode() >= 400) {
                    source.onError(new HttpException(response.statusCode(), body.toString()));
                    logger.warn("Failed to execute: {}", response.toString());
                } else {
                    source.onSuccess(body);
                }
            })).endHandler(closeHandler);
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
