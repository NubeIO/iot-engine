package com.nubeiot.dashboard.impl.handlers;

import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleBadRequestResponse;
import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleForbiddenResponse;

import java.util.List;
import java.util.UUID;

import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.common.utils.HttpException;
import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.core.utils.SQLUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.Role;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class BaseCollectionHandler {

    public void handleGetUrl(Message<Object> message, CustomMessage customMessage, MongoClient mongoClient,
                             JsonObject appConfig) {
        String collection = customMessage.getHeader().getString("collection");
        String siteId = customMessage.getHeader().getString("Site-Id");
        String id = customMessage.getHeader().getString("url");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(siteId)) {
                if (Strings.isNotBlank(id)) {
                    mongoClient.rxFindOne(collection, new JsonObject().put("site_id", siteId).put("id", id), null)
                               .subscribe(response -> {
                                   CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null,
                                                                                                (JsonObject) SQLUtils.getFirstNotNull(
                                                                                                    response,
                                                                                                    new JsonObject()),
                                                                                                HttpResponseStatus.OK.code());
                                   message.reply(replyMessage);
                               }, throwable -> handleException(message, throwable));
                } else {
                    mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId)).subscribe(response -> {
                        CustomMessage<List<JsonObject>> replyMessage = new CustomMessage<>(null, response,
                                                                                           HttpResponseStatus.OK.code());
                        message.reply(replyMessage);
                    }, throwable -> handleException(message, throwable));
                }
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        }
    }

    public void handlePostUrl(Message<Object> message, CustomMessage customMessage, MongoClient mongoClient) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        String siteId = customMessage.getHeader().getString("Site-Id");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(siteId)) {
                handlePostDocument(message, customMessage, mongoClient, collection, siteId);
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleForbiddenResponse(message);
        }
    }

    private void handlePostDocument(Message<Object> message, CustomMessage customMessage, MongoClient mongoClient,
                                    String collection, String siteId) {
        String id = UUID.randomUUID().toString();
        mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId).put("id", id)).map(response -> {
            JsonObject body = (JsonObject) customMessage.getBody();
            if (response.size() > 0) {
                throw new HttpException(HttpResponseStatus.CONFLICT.code(), "We have already that id value.");
            }
            body.put("site_id", siteId);
            body.put("id", id);
            return body;
        }).flatMap(body -> MongoUtils.postDocument(mongoClient, collection, body)).subscribe(buffer -> {
            CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null, new JsonObject(),
                                                                         HttpResponseStatus.OK.code());
            message.reply(replyMessage);
        }, throwable -> {
            HttpException exception = (HttpException) throwable;
            CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null, new JsonObject().put("message",
                                                                                                    exception.getMessage()),
                                                                         exception.getStatusCode().code());
            message.reply(replyMessage);
        });
    }

    public void handlePutUrl(Message<Object> message, CustomMessage customMessage, MongoClient mongoClient) {
        String role = customMessage.getHeader().getJsonObject("user").getString("role");
        String collection = customMessage.getHeader().getString("collection");
        String id = customMessage.getHeader().getString("url");
        String siteId = customMessage.getHeader().getString("Site-Id");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() == 0) {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        } else if (!role.equals(Role.GUEST.toString())) {
            if (sitesIds.contains(siteId)) {
                handlePutDocument(message, customMessage, mongoClient, collection, id, siteId);
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleForbiddenResponse(message);
        }
    }

    protected void handlePutDocument(Message<Object> message, CustomMessage customMessage, MongoClient mongoClient,
                                     String collection, String id, String siteId) {
        mongoClient.rxFindOne(collection, new JsonObject().put("site_id", siteId).put("id", id), null)
                   .map(jsonObject -> {
                       JsonObject body = (JsonObject) customMessage.getBody();
                       if (jsonObject != null) {
                           body.put("_id", jsonObject.getString("_id"));
                       }
                       body.put("site_id", siteId);
                       body.put("id", id);
                       return body;
                   })
                   .flatMap(body -> mongoClient.rxSave(collection, body))
                   .subscribe(buffer -> {
                       CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null, new JsonObject(),
                                                                                    HttpResponseStatus.OK.code());
                       message.reply(replyMessage);
                   }, throwable -> {
                       HttpException exception = (HttpException) throwable;
                       CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null,
                                                                                    new JsonObject().put("message",
                                                                                                         exception.getMessage()),
                                                                                    exception.getStatusCode().code());
                       message.reply(replyMessage);
                   });
    }

    public void handleDeleteUrl(Message<Object> message, CustomMessage customMessage, MongoClient mongoClient) {
        String collection = customMessage.getHeader().getString("collection");
        String id = customMessage.getHeader().getString("url");
        String siteId = customMessage.getHeader().getString("Site-Id");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(siteId)) {
                handleDeleteDocument(message, mongoClient, collection, id, siteId);
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleBadRequestResponse(message, "User must be associated with <SiteSetting>");
        }
    }

    protected void handleDeleteDocument(Message<Object> message, MongoClient mongoClient, String collection, String id,
                                        String siteId) {
        mongoClient.rxRemoveDocuments(collection, new JsonObject().put("site_id", siteId).put("id", id))
                   .subscribe(buffer -> {
                       CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null, new JsonObject(),
                                                                                    HttpResponseStatus.NO_CONTENT.code());
                       message.reply(replyMessage);
                   }, throwable -> handleException(message, throwable));
    }

    protected JsonArray getSitesIds(CustomMessage customMessage) {
        JsonObject user = customMessage.getHeader().getJsonObject("user");
        JsonArray sitesIds = user.getJsonArray("sites_ids", new JsonArray());
        if (sitesIds.size() == 0 && Strings.isNotBlank(user.getString("site_id"))) {
            sitesIds = new JsonArray().add(user.getString("site_id"));
        }
        return sitesIds;
    }

    protected void handleException(Message<Object> message, Throwable throwable) {
        HttpException exception = (HttpException) throwable;
        CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null, new JsonObject().put("message",
                                                                                                exception.getMessage()),
                                                                     exception.getStatusCode().code());
        message.reply(replyMessage);
    }

}
