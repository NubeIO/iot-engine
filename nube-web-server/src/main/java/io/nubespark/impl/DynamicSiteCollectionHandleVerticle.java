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

import static io.nubespark.constants.Address.DYNAMIC_SITE_COLLECTION_ADDRESS;
import static io.nubespark.utils.CustomMessageResponseHelper.*;

public class DynamicSiteCollectionHandleVerticle extends RxRestAPIVerticle {
    private Logger logger = LoggerFactory.getLogger(DynamicSiteCollectionHandleVerticle.class);

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void start() {
        super.start();
        EventBus eventBus = getVertx().eventBus();

        // Receive message
        eventBus.consumer(DYNAMIC_SITE_COLLECTION_ADDRESS, this::handleRequest);
    }

    private void handleRequest(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        String url = customMessage.getHeader().getString("url");

        if (url.equals("") && customMessage.getHeader().getString("method").equalsIgnoreCase("GET")) {
            // Getting all values; for example when we need to display all settings
            handleGetAll(message, customMessage);
        } else if (validateUrl(url)) {
            this.handleValidUrl(message, customMessage);
        } else {
            handleNotFoundResponse(message);
        }
    }

    private void handleGetAll(Message<Object> message, CustomMessage customMessage) {
        String collection = customMessage.getHeader().getString("collection");
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
                .flatMap(siteId -> dispatchRequests(HttpMethod.POST, URL.mongo_base_get_api + collection, new JsonObject().put("site_id", siteId)))
                .subscribe(buffer -> {
                    CustomMessage<JsonArray> replyMessage = new CustomMessage<>(
                        null,
                        buffer.toJsonArray(),
                        HttpResponseStatus.OK.code());
                    message.reply(replyMessage);
                }, throwable -> handleException(message, throwable));
        } else {
            handleBadRequestResponse(message, "User must be associated with <UserGroup> and <SiteSettings>");
        }
    }

    private void handleException(Message<Object> message, Throwable throwable) {
        logger.info("Cause: " + throwable.getCause());
        logger.info("Message: " + throwable.getMessage());
        HttpException exception = (HttpException) throwable;
        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
            null,
            new JsonObject().put("message", exception.getMessage()),
            exception.getStatusCode().code());
        message.reply(replyMessage);
    }

    private void handleValidUrl(Message<Object> message, CustomMessage customMessage) {
        String method = customMessage.getHeader().getString("method");
        switch (method.toUpperCase()) {
            case "GET":
                this.handleGetUrl(message, customMessage);
                break;
            case "POST":
                this.handlePostUrl(message, customMessage);
                break;
            case "PUT":
                this.handlePutUrl(message, customMessage);
                break;
            case "DELETE":
                this.handleDeleteUrl(message, customMessage);
                break;
            default:
                handleNotFoundResponse(message);
                break;
        }
    }

    private void handleGetUrl(Message<Object> message, CustomMessage customMessage) {
        String id = customMessage.getHeader().getString("url");
        String collection = customMessage.getHeader().getString("collection");
        String groupId = customMessage.getHeader().getJsonObject("user").getString("group_id");
        if (StringUtils.isNotNull(groupId)) {
            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + groupId, null)
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        return buffer.toJsonObject().getString("site_id");
                    } else {
                        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "May be your UserGroup is deleted from the System.");
                    }
                })
                .flatMap(siteId -> dispatchRequests(HttpMethod.POST, URL.mongo_base_get_api + collection, new JsonObject().put("site_id", siteId).put("id", id)))
                .subscribe(buffer -> {
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        this.pickOneOrNullJsonObject(buffer.toJsonArray()),
                        HttpResponseStatus.OK.code());
                    message.reply(replyMessage);
                }, throwable -> handleException(message, throwable));
        } else {
            handleBadRequestResponse(message, "User must be associated with <UserGroup> and <SiteSettings>");
        }
    }

    private void handlePostUrl(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String id = customMessage.getHeader().getString("url");
        String collection = customMessage.getHeader().getString("collection");
        String groupId = customMessage.getHeader().getJsonObject("user").getString("group_id");
        if (StringUtils.isNull(groupId)) {
            handleBadRequestResponse(message, "User must be associated with <UserGroup> and <SiteSettings>");
        } else if (!role.equals(Role.GUEST.toString())) {
            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + groupId, null)
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        return buffer.toJsonObject().getString("site_id");
                    } else {
                        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "May be your UserGroup is deleted from the System.");
                    }
                })
                .flatMap(siteId -> dispatchRequests(HttpMethod.POST, URL.mongo_base_get_api + collection, new JsonObject().put("site_id", siteId).put("id", id))
                    .map(buffer -> {
                        JsonObject body = (JsonObject) customMessage.getBody();
                        if (buffer.toJsonArray().size() > 0) {
                            throw new HttpException(HttpResponseStatus.CONFLICT.code(), "We have already that id value.");
                        }
                        body.put("site_id", siteId);
                        body.put("id", id);
                        return body;
                    }))
                .flatMap(body -> dispatchRequests(HttpMethod.POST, URL.mongo_base_post_api + collection, body))
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

    private void handlePutUrl(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String id = customMessage.getHeader().getString("url");
        String collection = customMessage.getHeader().getString("collection");
        String groupId = customMessage.getHeader().getJsonObject("user").getString("group_id");
        if (StringUtils.isNull(groupId)) {
            handleBadRequestResponse(message, "User must be associated with <UserGroup> and <SiteSettings>");
        } else if (!role.equals(Role.GUEST.toString())) {
            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + groupId, null)
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        return buffer.toJsonObject().getString("site_id");
                    } else {
                        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "May be your UserGroup is deleted from the System.");
                    }
                })
                .flatMap(siteId -> dispatchRequests(HttpMethod.POST, URL.mongo_base_get_api + collection, new JsonObject().put("site_id", siteId).put("id", id))
                    .map(buffer -> {
                        JsonArray jsonArray = buffer.toJsonArray();
                        JsonObject body = (JsonObject) customMessage.getBody();
                        if (jsonArray.size() > 0) {
                            body.put("_id", this.pickOneOrNullJsonObject(buffer.toJsonArray()).getString("_id"));
                        }
                        body.put("site_id", siteId);
                        body.put("id", id);
                        return body;
                    }))
                .flatMap(body -> dispatchRequests(HttpMethod.PUT, URL.mongo_base_put_api + collection, body))
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

    private void handleDeleteUrl(Message<Object> message, CustomMessage customMessage) {
        String id = customMessage.getHeader().getString("url");
        String collection = customMessage.getHeader().getString("collection");
        String groupId = customMessage.getHeader().getJsonObject("user").getString("group_id");
        if (StringUtils.isNotNull(groupId)) {
            dispatchRequests(HttpMethod.GET, URL.get_user_group + "/" + groupId, null)
                .map(buffer -> {
                    if (StringUtils.isNotNull(buffer.toString())) {
                        return buffer.toJsonObject().getString("site_id");
                    } else {
                        throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "May be your UserGroup is deleted from the System.");
                    }
                })
                .flatMap(siteId -> dispatchRequests(HttpMethod.POST, URL.mongo_base_delete_api + collection, new JsonObject().put("site_id", siteId).put("id", id)))
                .subscribe(buffer -> {
                    int statusCode = HttpResponseStatus.NO_CONTENT.code();
                    if (StringUtils.isNotNull(buffer.toString())) {
                        statusCode = buffer.getDelegate().toJsonObject().getInteger("statusCode");
                    }
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        new JsonObject(),
                        statusCode);
                    message.reply(replyMessage);
                }, throwable -> handleException(message, throwable));
        } else {
            handleBadRequestResponse(message, "User must be associated with <UserGroup> and <SiteSettings>");
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
