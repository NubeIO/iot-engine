package com.nubeiot.dashboard.impl.handlers;

import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleBadRequestResponse;
import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleForbiddenResponse;

import java.util.List;

import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.common.utils.SQLUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.utils.MultiTenantCustomMessageHelper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class WidgetImageCollectionHandler extends BaseCollectionHandler {

    private static final String IMAGE_FIELD_NAME = "image";

    @SuppressWarnings("Duplicates")
    public void handleGetUrl(Message<Object> message, CustomMessage customMessage, MongoClient mongoClient) {
        String collection = customMessage.getHeader().getString("collection");
        String siteId = customMessage.getHeader().getString("Site-Id");
        String id = customMessage.getHeader().getString("url");
        JsonArray sitesIds = getSitesIds(customMessage);
        if (sitesIds.size() > 0) {
            if (sitesIds.contains(siteId)) {
                if (Strings.isNotBlank(id)) {
                    mongoClient.rxFindOne(collection, new JsonObject().put("site_id", siteId).put("id", id), null)
                               .subscribe(response -> {
                                   JsonObject jsonObject = SQLUtils.getFirstNotNull(response, new JsonObject());
                                   buildSiteWithAbsoluteImageUri(message, jsonObject, IMAGE_FIELD_NAME);
                                   CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null, jsonObject,
                                                                                                HttpResponseStatus.OK.code());
                                   message.reply(replyMessage);
                               }, throwable -> handleException(message, throwable));
                } else {
                    mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId)).subscribe(response -> {
                        response.forEach(
                            jsonObject -> buildSiteWithAbsoluteImageUri(message, jsonObject, IMAGE_FIELD_NAME));
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

    private void buildSiteWithAbsoluteImageUri(Message<Object> message, JsonObject jsonObject, String key) {
        if (Strings.isNotBlank(jsonObject.getString(key))) {
            jsonObject.put(key, MultiTenantCustomMessageHelper.buildAbsoluteUri(message, jsonObject.getString(key)));
        }
    }

}
