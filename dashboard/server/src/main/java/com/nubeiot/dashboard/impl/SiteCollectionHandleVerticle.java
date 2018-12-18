package com.nubeiot.dashboard.impl;

import com.nubeiot.core.common.RxRestAPIVerticle;
import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.common.utils.HttpException;
import com.nubeiot.core.common.utils.SQLUtils;
import com.nubeiot.core.common.utils.StringUtils;
import com.nubeiot.dashboard.Role;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.util.List;

import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.*;
import static com.nubeiot.dashboard.constants.Address.SITE_COLLECTION_ADDRESS;

public class SiteCollectionHandleVerticle extends RxRestAPIVerticle {
    private Logger logger = LoggerFactory.getLogger(SiteCollectionHandleVerticle.class);
    private MongoClient mongoClient;

    @Override
    public void start() {
        super.start();
        mongoClient = MongoClient.createNonShared(vertx, appConfig.getJsonObject("mongo").getJsonObject("config"));
        EventBus eventBus = getVertx().eventBus();

        // Receive message
        eventBus.consumer(SITE_COLLECTION_ADDRESS, this::handleRequest);
    }

    private void handleRequest(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        String url = customMessage.getHeader().getString("url");

        if (!validateData(customMessage)) {
            handleForbiddenResponse(message);
        }

        String method = customMessage.getHeader().getString("method");
        switch (method.toUpperCase()) {
            case "GET":
                if (StringUtils.isNull(url)) {
                    this.handleGetAll(message, customMessage);
                } else {
                    this.handleGetId(message, customMessage);
                }
                break;
            case "POST":
                this.handlePost(message, customMessage);
                break;
            case "PATCH":
                this.handlePatch(message, customMessage);
                break;
            case "DELETE":
                this.handleDeleteUrl(message, customMessage);
                break;
            default:
                handleNotFoundResponse(message);
                break;
        }
    }

    private JsonArray getSitesIds(CustomMessage customMessage) {
        JsonObject user = customMessage.getHeader().getJsonObject("user");
        JsonArray sitesIds = user.getJsonArray("sites_ids", new JsonArray());
        if (sitesIds.size() == 0 && StringUtils.isNotNull(user.getString("site_id"))) {
            sitesIds = new JsonArray().add(user.getString("site_id"));
        }
        return sitesIds;
    }

    @SuppressWarnings("Duplicates")
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

    private void handleGetAll(Message<Object> message, CustomMessage customMessage) {
        String collection = customMessage.getHeader().getString("collection");
        JsonArray sitesIds = getSitesIds(customMessage);
        String siteId = customMessage.getHeader().getString("Site-Id");

        // noinspection Duplicates
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(siteId)) {
                mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId))
                    .subscribe(response -> {
                        CustomMessage<List<JsonObject>> replyMessage = new CustomMessage<>(
                            null,
                            response,
                            HttpResponseStatus.OK.code());
                        message.reply(replyMessage);
                    }, throwable -> handleException(message, throwable));
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        }
    }

    private void handleGetId(Message<Object> message, CustomMessage customMessage) {
        String collection = customMessage.getHeader().getString("collection");
        String siteId = customMessage.getHeader().getString("Site-Id");
        String id = customMessage.getHeader().getString("url");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(siteId)) {
                mongoClient.rxFindOne(collection, new JsonObject().put("_id", id), null)
                    .subscribe(response -> {
                        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                            null,
                            SQLUtils.getFirstNotNull(response, new JsonObject()),
                            HttpResponseStatus.OK.code());
                        message.reply(replyMessage);
                    }, throwable -> handleException(message, throwable));
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        }
    }

    private void handlePost(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        String url = customMessage.getHeader().getString("url");
        String siteId = customMessage.getHeader().getString("Site-Id");

        if (StringUtils.isNotNull(url)) {
            handleNotFoundResponse(message);
        }

        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(siteId)) {
                JsonObject body = (JsonObject) customMessage.getBody();
                body.put("site_id", siteId);
                mongoClient.rxSave(collection, body)
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
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePatch(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        String id = customMessage.getHeader().getString("url");
        String siteId = customMessage.getHeader().getString("Site-Id");
        JsonArray sitesIds = getSitesIds(customMessage);


        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(siteId)) {
                mongoClient.rxFindOne(collection, new JsonObject().put("site_id", siteId).put("_id", id), null)
                    .map(jsonObject -> {
                        JsonObject body = (JsonObject) customMessage.getBody();
                        body.put("site_id", siteId);
                        body.put("_id", id);
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
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handleDeleteUrl(Message<Object> message, CustomMessage customMessage) {
        String collection = customMessage.getHeader().getString("collection");
        String id = customMessage.getHeader().getString("url");
        String siteId = customMessage.getHeader().getString("Site-Id");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(siteId)) {
                mongoClient.rxRemoveDocuments(collection, new JsonObject().put("site_id", siteId).put("_id", id))
                    .subscribe(buffer -> {
                        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                            null,
                            new JsonObject(),
                            HttpResponseStatus.NO_CONTENT.code());
                        message.reply(replyMessage);
                    }, throwable -> handleException(message, throwable));
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        }
    }

    private boolean validateData(CustomMessage customMessage) {
        return StringUtils.isNotNull(customMessage.getHeader().getString("Site-Id"));
    }
}
