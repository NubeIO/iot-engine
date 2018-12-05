package com.nubeiot.core.common.utils;

import com.nubeiot.core.common.utils.response.ResponseUtils;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * @deprecated use {@link ErrorMessage} and @{@link HttpStatusMapping}
 */
@Deprecated
public class ErrorHandler {

    public static void handleError(Throwable throwable, RoutingContext routingContext) {
        if (throwable instanceof ErrorCodeException) {
            switch (((ErrorCodeException) throwable).getErrorCodes()) {
                case BAD_ACTION:
                    routingContext.response()
                                  .setStatusCode(403)
                                  .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                                  .end(Json.encodePrettily(new JsonObject().put("message",
                                                                                "You do not have permission to run " +
                                                                                "this query.")));
                    break;
                case NO_QUERY_SPECIFIED:
                    routingContext.response()
                                  .setStatusCode(400)
                                  .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                                  .end(Json.encodePrettily(new JsonObject().put("message",
                                                                                "Request must have a valid JSON body " +
                                                                                "with 'query' field.")));
                    break;
            }
        } else {
            routingContext.response()
                          .setStatusCode(500)
                          .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                          .end(Json.encodePrettily(
                                  new JsonObject().put("message", "Server Error: " + throwable.getMessage())));
        }
    }

}
