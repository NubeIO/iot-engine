package com.nubeiot.dashboard.impl;

import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleForbiddenResponse;
import static com.nubeiot.core.common.utils.CustomMessageResponseHelper.handleNotFoundResponse;
import static com.nubeiot.dashboard.constants.Address.DYNAMIC_SITE_COLLECTION_ADDRESS;

import com.nubeiot.core.common.RxRestAPIVerticle;
import com.nubeiot.core.common.utils.CustomMessage;
import com.nubeiot.core.common.utils.StringUtils;
import com.nubeiot.dashboard.enums.DynamicCollection;
import com.nubeiot.dashboard.impl.handlers.BaseCollectionHandler;

import io.reactivex.Single;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.reactivex.ext.mongo.MongoClient;

public class DynamicSiteCollectionHandleVerticle extends RxRestAPIVerticle {

    private MongoClient mongoClient;

    @Override
    protected Single<String> onStartComplete() {
        mongoClient = MongoClient.createNonShared(vertx, this.appConfig.getJsonObject("mongo").getJsonObject("config"));
        EventBus eventBus = getVertx().eventBus();

        // Receive message
        eventBus.consumer(DYNAMIC_SITE_COLLECTION_ADDRESS, this::handleRequest);
        return Single.just("Deployed successfully...");
    }

    private void handleRequest(Message<Object> message) {
        CustomMessage customMessage = (CustomMessage) message.body();
        String url = customMessage.getHeader().getString("url");

        if (validateUrl(url)) {
            if (validateData(customMessage)) {
                this.handleValidUrl(message, customMessage);
            } else {
                handleForbiddenResponse(message);
            }
        } else {
            handleNotFoundResponse(message);
        }
    }

    private void handleValidUrl(Message<Object> message, CustomMessage customMessage) {
        String method = customMessage.getHeader().getString("method");
        BaseCollectionHandler baseCollection = DynamicCollection.getCollection(
            customMessage.getHeader().getString("collection"));
        switch (method.toUpperCase()) {
            case "GET":
                baseCollection.handleGetUrl(message, customMessage, mongoClient);
                break;
            case "POST":
                baseCollection.handlePostUrl(message, customMessage, mongoClient);
                break;
            case "PUT":
                baseCollection.handlePutUrl(message, customMessage, mongoClient);
                break;
            case "DELETE":
                baseCollection.handleDeleteUrl(message, customMessage, mongoClient);
                break;
            default:
                handleNotFoundResponse(message);
                break;
        }
    }

    private boolean validateUrl(String url) {
        return !url.contains("?");
    }

    private boolean validateData(CustomMessage customMessage) {
        return StringUtils.isNotNull(customMessage.getHeader().getString("Site-Id"));
    }

}
