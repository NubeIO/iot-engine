package io.nubespark.vertx.common;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.controller.HttpException;
import io.nubespark.utils.response.ResponseUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public class HttpHelper {
    public static void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    public static void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }

    public static void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    public static void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_implemented").encodePrettily());
    }

    public static void badGateway(Throwable ex, RoutingContext context) {
        ex.printStackTrace();
        context.response()
                .setStatusCode(502)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Bad Gateway")
                        .put("message", ex.getMessage())
                        .encodePrettily());
    }

    public static void serviceUnavailable(RoutingContext context) {
        context.fail(503);
    }

    public static void serviceUnavailable(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    public static void serviceUnavailable(RoutingContext context, String cause) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", cause).encodePrettily());
    }

    public static void failAuthentication(RoutingContext ctx) {
        ctx.response().setStatusCode(401)
                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonObject().put("message", "Unauthorized")));
    }

    public static void forbidden(RoutingContext ctx) {
        ctx.response().setStatusCode(HttpResponseStatus.FORBIDDEN.code())
                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonObject().put("message", "You are not authorized to perform this action")));
    }

    public static Throwable notFound() {
        return new Throwable("Not Found");
    }

    public static HttpException forbidden() {
        return new HttpException(HttpResponseStatus.FORBIDDEN, "You don't have permission to perform the action.");
    }

    public static HttpException badRequest(String message) {
        return new HttpException(HttpResponseStatus.BAD_REQUEST, message);
    }
}
