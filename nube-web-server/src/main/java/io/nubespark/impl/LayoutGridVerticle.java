package io.nubespark.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.Role;
import io.nubespark.controller.HttpException;
import io.nubespark.utils.CustomMessage;
import io.nubespark.utils.StringUtils;
import io.nubespark.utils.URL;
import io.nubespark.vertx.common.RxRestAPIVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import static io.nubespark.constants.Address.LAYOUT_GRID_ADDRESS;
import static io.nubespark.utils.CustomMessageResponseHelper.*;

public class LayoutGridVerticle extends RxRestAPIVerticle {
    private Logger logger = LoggerFactory.getLogger(LayoutGridVerticle.class);

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void start() {
        super.start();
        EventBus eventBus = getVertx().eventBus();

        // Receive message
        eventBus.consumer(LAYOUT_GRID_ADDRESS, this::handleRequest);
    }

    private void handleRequest(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        String url = customMessage.getHeader().getString("url");

        if (validateUrl(url)) {
            this.handleValidUrl(message, customMessage);
        } else {
            handleNotFoundResponse(message);
        }
    }

    private void handleValidUrl(Message<Object> message, CustomMessage customMessage) {
        String method = customMessage.getHeader().getString("method");
        switch (method.toUpperCase()) {
            case "GET":
                this.handleGetUrl(message, customMessage);
                break;
            case "PUT":
                this.handlePutUrl(message, customMessage);
                break;
            default:
                handleNotFoundResponse(message);
                break;
        }
    }

    private void handleGetUrl(Message<Object> message, CustomMessage customMessage) {
        String layout = customMessage.getHeader().getString("url");
        String groupId = customMessage.getHeader().getJsonObject("user").getString("group_id");
        if (StringUtils.isNotNull(groupId)) {
            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + groupId, null)
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        return buffer.toJsonObject().getString("site_id");
                    } else {
                        throw new HttpException(404, "May be your UserGroup is deleted from the System.");
                    }
                })
                .flatMap(siteId -> dispatchRequests(HttpMethod.POST, URL.get_layout_grid, new JsonObject().put("site_id", siteId).put("layout", layout)))
                .subscribe(buffer -> {
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        this.pickOneOrNullJsonObject(buffer.toJsonArray()),
                        HttpResponseStatus.OK.code());
                    message.reply(replyMessage);
                }, throwable -> {
                    logger.info("Cause: " + throwable.getCause());
                    logger.info("Message: " + throwable.getMessage());
                    HttpException exception = (HttpException) throwable;
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        new JsonObject().put("message", exception.getMessage()),
                        exception.getStatusCode().code());
                    message.reply(replyMessage);
                });
        } else {
            handleBadRequestResponse(message, "User must be associated with <UserGroup> and <SiteSettings>");
        }
    }

    private void handlePutUrl(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String layout = customMessage.getHeader().getString("url");
        String groupId = customMessage.getHeader().getJsonObject("user").getString("group_id");
        if (StringUtils.isNull(groupId)) {
            handleBadRequestResponse(message, "User must be associated with <UserGroup> and <SiteSettings>");
        } else if (!role.equals(Role.GUEST.toString())) {
            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + groupId, null)
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        return buffer.toJsonObject().getString("site_id");
                    } else {
                        throw new HttpException(404, "May be your UserGroup is deleted from the System.");
                    }
                })
                .flatMap(siteId -> dispatchRequests(HttpMethod.POST, URL.get_layout_grid, new JsonObject().put("site_id", siteId).put("layout", layout))
                    .map(buffer -> {
                        JsonArray jsonArray = buffer.toJsonArray();
                        JsonObject body = (JsonObject) customMessage.getBody();
                        if (jsonArray.size() > 0) {
                            body.put("_id", this.pickOneOrNullJsonObject(buffer.toJsonArray()).getString("_id"));
                        }
                        body.put("site_id", siteId);
                        body.put("layout", layout);
                        return body;
                    }))
                .flatMap(body -> dispatchRequests(HttpMethod.PUT, URL.put_layout_grid, body))
                .subscribe(buffer -> {
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        new JsonObject(),
                        HttpResponseStatus.OK.code());
                    message.reply(replyMessage);
                }, throwable -> {
                    HttpException exception = (HttpException) throwable;
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        new JsonObject().put("message", exception.getMessage()),
                        exception.getStatusCode().code());
                    message.reply(replyMessage);
                });
        } else {
            handleForbiddenResponse(message);
        }
    }

    private JsonObject pickOneOrNullJsonObject(JsonArray jsonArray) {
        if (jsonArray.size() > 0) {
            return jsonArray.getJsonObject(0);
        } else {
            return new JsonObject();
        }
    }

    private boolean validateUrl(String url) {
        return !(url.contains("/") || url.contains("?"));
    }
}
