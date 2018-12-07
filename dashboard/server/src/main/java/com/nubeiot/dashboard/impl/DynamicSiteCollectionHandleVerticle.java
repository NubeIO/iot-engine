package com.nubeiot.dashboard.impl;

import com.nubeiot.core.common.RxRestAPIVerticle;
import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.common.utils.HttpException;
import com.nubeiot.core.common.utils.StringUtils;
import com.nubeiot.dashboard.Role;
import com.nubeiot.dashboard.utils.MongoUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.util.List;

import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.*;
import static com.nubeiot.dashboard.constants.Address.DYNAMIC_SITE_COLLECTION_ADDRESS;

public class DynamicSiteCollectionHandleVerticle extends RxRestAPIVerticle {

    private MongoClient mongoClient;

    @Override
    public void start() {
        super.start();
        mongoClient = MongoClient.createNonShared(vertx, appConfig.getJsonObject("mongo").getJsonObject("config"));
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
            if (validateData(customMessage)) {
                this.handleValidUrl(message, customMessage);
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleNotFoundResponse(message);
        }
    }

    private void handleGetAll(Message<Object> message, CustomMessage customMessage) {
        String collection = customMessage.getHeader().getString("collection");
        JsonArray sitesIds = getSitesIds(customMessage);
        String siteId = customMessage.getHeader().getString("Site-Id");

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

    private JsonArray getSitesIds(CustomMessage customMessage) {
        JsonObject user = customMessage.getHeader().getJsonObject("user");
        JsonArray sitesIds = user.getJsonArray("sites_ids", new JsonArray());
        if (sitesIds.size() == 0 && StringUtils.isNotNull(user.getString("site_id"))) {
            sitesIds = new JsonArray().add(user.getString("site_id"));
        }
        return sitesIds;
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
        String collection = customMessage.getHeader().getString("collection");
        String siteId = customMessage.getHeader().getString("Site-Id");
        String id = customMessage.getHeader().getString("url");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(siteId)) {
                mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id))
                    .subscribe(response -> {
                        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(
                            null,
                            this.pickOneOrNullJsonObject(response),
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

    private void handlePostUrl(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        String id = customMessage.getHeader().getString("url");
        String siteId = customMessage.getHeader().getString("Site-Id");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(siteId)) {
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
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePutUrl(Message<Object> message, CustomMessage customMessage) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        String id = customMessage.getHeader().getString("url");
        String siteId = customMessage.getHeader().getString("Site-Id");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(siteId)) {
                mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id))
                    .map(jsonArray -> {
                        JsonObject body = (JsonObject) customMessage.getBody();
                        if (jsonArray.size() > 0) {
                            body.put("_id", this.pickOneOrNullJsonObject(jsonArray).getString("_id"));
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
                mongoClient.rxRemoveDocuments(collection, new JsonObject().put("site_id", siteId).put("id", id))
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

    private JsonObject pickOneOrNullJsonObject(List<JsonObject> jsonObjectList) {
        if (jsonObjectList.size() > 0) {
            return jsonObjectList.get(0);
        } else {
            return new JsonObject();
        }
    }

    private boolean validateUrl(String url) {
        return !(url.contains("/") || url.contains("?"));
    }

    private boolean validateData(CustomMessage customMessage) {
        return StringUtils.isNotNull(customMessage.getHeader().getString("Site-Id"));
    }
}
