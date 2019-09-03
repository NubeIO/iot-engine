package com.nubeiot.dashboard.connector.ditto;

import io.reactivex.SingleEmitter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.client.handler.HttpLightResponseBodyHandler;

import lombok.NonNull;

/**
 * Represents for handler {@code HTTP Response}
 */
public class HttpDittoResponseBodyHandler extends HttpLightResponseBodyHandler {

    public HttpDittoResponseBodyHandler(@NonNull HttpClientResponse response,
                                        @NonNull SingleEmitter<ResponseData> emitter, boolean swallowError) {
        super(response, emitter, swallowError);
    }

    @Override
    protected JsonObject tryParse(Buffer buffer) {
        return JsonData.tryParse(buffer, false, "data", true).toJson();
    }

}
