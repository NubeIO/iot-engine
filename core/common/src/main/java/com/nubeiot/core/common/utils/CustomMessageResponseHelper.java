package com.nubeiot.core.common.utils;

import com.nubeiot.core.exceptions.HttpStatusMapping;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * @deprecated use @{@link HttpStatusMapping}
 */
@Deprecated
public class CustomMessageResponseHelper {

    public static void handleNotFoundResponse(Message<Object> message) {
        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(new JsonObject().put("message", "Not Implemented"),
                                                                     HttpResponseStatus.NOT_FOUND.code());
        message.reply(replyMessage);
    }

    public static void handleForbiddenResponse(Message<Object> message) {
        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(new JsonObject(),
                                                                     HttpResponseStatus.FORBIDDEN.code());
        message.reply(replyMessage);
    }

    public static void handleBadRequestResponse(Message<Object> message, String msg) {
        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(new JsonObject().put("message", msg),
                                                                     HttpResponseStatus.BAD_REQUEST.code());
        message.reply(replyMessage);
    }

    public static void handleHttpException(Message<Object> message, Throwable throwable) {
        HttpException exception = (HttpException) throwable;
        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                new JsonObject().put("message", exception.getMessage()), exception.getStatusCode().code());
        message.reply(replyMessage);
    }

}
