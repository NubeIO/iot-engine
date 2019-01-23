package com.nubeiot.dashboard.impl.handlers;

import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleBadRequestResponse;
import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleForbiddenResponse;

import java.util.List;

import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.dashboard.helpers.CustomMessageHelper;
import com.nubeiot.dashboard.utils.MongoResourceUtils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class WidgetImageCollectionHandler extends BaseCollectionHandler {

    private static final String IMAGE_FIELD = "image";

    @SuppressWarnings("Duplicates")
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
                               .flatMap(widgetImage -> {
                                   return MongoResourceUtils.putAbsPath(mongoClient,
                                                                        CustomMessageHelper.getHost(message),
                                                                        widgetImage, IMAGE_FIELD,
                                                                        appConfig.getString("MEDIA_ROOT"));
                               })
                               .subscribe(widgetImage -> {
                                   CustomMessage<JsonObject> replyMessage = new CustomMessage<>(null, widgetImage,
                                                                                                HttpResponseStatus.OK.code());
                                   message.reply(replyMessage);
                               }, throwable -> handleException(message, throwable));
                } else {
                    mongoClient.rxFind(collection, new JsonObject().put("site_id", siteId)).flatMap(widgetImages -> {
                        return Observable.fromIterable(widgetImages).flatMapSingle(widgetImage -> {
                            return MongoResourceUtils.putAbsPath(mongoClient, CustomMessageHelper.getHost(message),
                                                                 widgetImage, IMAGE_FIELD,
                                                                 appConfig.getString("MEDIA_ROOT"));
                        }).toList();
                    }).subscribe(widgetImages -> {
                        CustomMessage<List<JsonObject>> replyMessage = new CustomMessage<>(null, widgetImages,
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

}
