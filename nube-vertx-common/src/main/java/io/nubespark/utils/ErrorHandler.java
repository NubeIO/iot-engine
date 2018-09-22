package io.nubespark.utils;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

public class ErrorHandler {
    public static void handleError(Throwable throwable, RoutingContext routingContext) {
        if (throwable instanceof ErrorCodeException) {
            switch (((ErrorCodeException) throwable).getErrorCodes()) {
                case BAD_ACTION:
                    routingContext.response()
                        .setStatusCode(403)
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encodePrettily(new JsonObject().put("message", "You do not have permission to run this query.")));
                    break;
                case NO_QUERY_SPECIFIED:
                    routingContext.response()
                        .setStatusCode(400)
                        .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .end(Json.encodePrettily(new JsonObject().put("message", "Request must have a valid JSON body with 'query' field.")));
                    break;
            }
        } else {
            routingContext.response()
                .setStatusCode(500)
                .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonObject().put("message", "Server Error: " + throwable.getMessage())));
        }
    }
}
