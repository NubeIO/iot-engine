package com.nubeiot.core.mongo;

import io.vertx.reactivex.ext.mongo.MongoClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestMongoClientProvider {

    @Getter
    private final MongoClient mongoClient;

}
