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
import io.vertx.core.json.JsonArray;
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

        if (StringUtils.isNotNull(url) && !url.contains("/") && customMessage.getHeader().getString("method").equalsIgnoreCase("GET")) {
            // Getting all values; for example when we need to display all settings
            handleGetAll(message, customMessage, url);
        } else if (validateUrl(url)) {
            this.handleValidUrl(message, customMessage);
        } else {
            handleNotFoundResponse(message);
        }
    }

    private void handleGetAll(Message<Object> message, CustomMessage customMessage, String siteId) {
        String collection = customMessage.getHeader().getString("collection");
        JsonArray sitesIds = getSitesIds(customMessage);

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
        URLParser urlParser = new URLParser(customMessage.getHeader().getString("url"));
        String collection = customMessage.getHeader().getString("collection");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(urlParser.getSiteId())) {
                mongoClient.rxFind(collection, new JsonObject().put("site_id", urlParser.getSiteId()).put("id", urlParser.getId()))
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
        URLParser urlParser = new URLParser(customMessage.getHeader().getString("url"));
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(urlParser.getSiteId())) {
                mongoClient.rxFind(collection, new JsonObject().put("site_id", urlParser.getSiteId()).put("id", urlParser.getId()))
                    .map(response -> {
                        JsonObject body = (JsonObject) customMessage.getBody();
                        if (response.size() > 0) {
                            throw new HttpException(HttpResponseStatus.CONFLICT.code(), "We have already that id value.");
                        }
                        body.put("site_id", urlParser.getSiteId());
                        body.put("id", urlParser.getId());
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
        URLParser urlParser = new URLParser(customMessage.getHeader().getString("url"));
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(urlParser.getSiteId())) {
                mongoClient.rxFind(collection, new JsonObject().put("site_id", urlParser.getSiteId()).put("id", urlParser.getId()))
                    .map(jsonArray -> {
                        JsonObject body = (JsonObject) customMessage.getBody();
                        if (jsonArray.size() > 0) {
                            body.put("_id", this.pickOneOrNullJsonObject(jsonArray).getString("_id"));
                        }
                        body.put("site_id", urlParser.getSiteId());
                        body.put("id", urlParser.getId());
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
        URLParser urlParser = new URLParser(customMessage.getHeader().getString("url"));
        String collection = customMessage.getHeader().getString("collection");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(urlParser.getSiteId())) {
                mongoClient.rxRemoveDocuments(collection, new JsonObject().put("site_id", urlParser.getSiteId()).put("id", urlParser.getId()))
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
        // We must need to have '<site_id>/<id>'
        return url.contains("/") && !url.contains("?");
    }


    class URLParser {
        private String siteId;
        private String id;

        URLParser(String url) {
            String[] values = url.split("/");
            siteId = values[0];
            if (values.length > 1) {
                id = values[1];
            } else {
                id = "";
            }
        }

        public String getSiteId() {
            return siteId;
        }

        public String getId() {
            return id;
        }
    }
}
