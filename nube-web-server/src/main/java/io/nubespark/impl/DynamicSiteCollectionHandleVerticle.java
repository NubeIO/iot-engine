package io.nubespark.impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.nubespark.Role;
import io.nubespark.utils.CustomMessage;
import io.nubespark.utils.HttpException;
import io.nubespark.utils.MongoUtils;
import io.nubespark.utils.StringUtils;
import io.nubespark.vertx.common.RxRestAPIVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.util.List;

import static io.nubespark.constants.Address.DYNAMIC_SITE_COLLECTION_ADDRESS;
import static io.nubespark.utils.CustomMessageResponseHelper.*;

public class DynamicSiteCollectionHandleVerticle extends RxRestAPIVerticle {
    private Logger logger = LoggerFactory.getLogger(DynamicSiteCollectionHandleVerticle.class);
    private MongoClient mongoClient;

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void start() {
        super.start();
        mongoClient = MongoClient.createNonShared(vertx, config().getJsonObject("mongo").getJsonObject("config"));
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
        String siteId = customMessage.getHeader().getJsonObject("user").getString("site_id");
        if (StringUtils.isNotNull(siteId)) {
            mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId))
                .subscribe(response -> {
                    CustomMessage<List<JsonObject>> replyMessage = new CustomMessage<>(
                        null,
                        response,
                        HttpResponseStatus.OK.code());
                    message.reply(replyMessage);
                }, throwable -> handleException(message, throwable));
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
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
        String siteId = customMessage.getHeader().getJsonObject("user").getString("site_id");
        if (StringUtils.isNotNull(siteId)) {
            mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id))
                .subscribe(response -> {
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        MongoUtils.pickOneOrNullJsonObject(response),
                        HttpResponseStatus.OK.code());
                    message.reply(replyMessage);
                }, throwable -> handleException(message, throwable));
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        }
    }

    private void handlePostUrl(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String id = customMessage.getHeader().getString("url");
        String collection = customMessage.getHeader().getString("collection");
        String siteId = customMessage.getHeader().getJsonObject("user").getString("site_id");
        if (StringUtils.isNull(siteId)) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id))
                .map(response -> {
                    JsonObject body = (JsonObject) customMessage.getBody();
                    if (response.size() > 0) {
                        throw new HttpException(HttpResponseStatus.CONFLICT.code(), "We have already that id value.");
                    }
                    body.put("site_id", siteId);
                    body.put("id", id);
                    return body;
                })
                .flatMap(body -> MongoUtils.postDocument(mongoClient, collection, body))
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
        String siteId = customMessage.getHeader().getJsonObject("user").getString("site_id");
        if (StringUtils.isNull(siteId)) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id))
                .map(jsonArray -> {
                    JsonObject body = (JsonObject) customMessage.getBody();
                    if (jsonArray.size() > 0) {
                        body.put("_id", MongoUtils.pickOneOrNullJsonObject(jsonArray).getString("_id"));
                    }
                    body.put("site_id", siteId);
                    body.put("id", id);
                    return body;
                })
                .flatMap(body -> mongoClient.rxSave(collection, body))
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
        String siteId = customMessage.getHeader().getJsonObject("user").getString("site_id");
        if (StringUtils.isNotNull(siteId)) {
            mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id))
                .subscribe(buffer -> {
                    CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                        null,
                        new JsonObject(),
                        HttpResponseStatus.NO_CONTENT.code());
                    message.reply(replyMessage);
                }, throwable -> handleException(message, throwable));
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        }
    }

    private boolean validateUrl(String url) {
        return !(url.contains("/") || url.contains("?"));
    }
}
