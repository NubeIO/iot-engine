package com.nubeiot.edge.connector.datapoint.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.functions.Consumer;
import io.vertx.core.json.JsonObject;

public interface Publisher extends Consumer<JsonObject> {

    static <T extends VertxPojo> Publisher create(Class<T> clazz) {
        return null;
    }

}
