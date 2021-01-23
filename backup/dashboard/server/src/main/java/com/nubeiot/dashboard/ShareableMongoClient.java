package com.nubeiot.dashboard;

import io.vertx.core.shareddata.Shareable;
import io.vertx.reactivex.ext.mongo.MongoClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ShareableMongoClient implements Shareable {

    final static String SHARABLE_MONGO_CLIENT_DATA_KEY = "SHARABLE_MONGO_CLIENT";

    @Getter
    private final MongoClient mongoClient;

}
