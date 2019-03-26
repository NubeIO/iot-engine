package com.nubeiot.dashboard.helpers;

import static com.nubeiot.core.http.ApiConstants.DEFAULT_CONTENT_TYPE;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.ApiConstants;

import io.vertx.core.json.JsonObject;

public class ResponseDataHelper {

    public static ResponseData unauthorized() {
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(401)
                    .setHeaders(new JsonObject().put(ApiConstants.CONTENT_TYPE, DEFAULT_CONTENT_TYPE))
                    .setBodyMessage(new JsonObject().put("message", "Unauthorized").encode());
        return responseData;
    }

}
