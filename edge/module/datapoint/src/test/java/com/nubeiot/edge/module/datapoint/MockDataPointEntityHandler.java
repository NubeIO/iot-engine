package com.nubeiot.edge.module.datapoint;

import org.jooq.Configuration;

import io.vertx.core.Vertx;

import lombok.NonNull;

public class MockDataPointEntityHandler extends DataPointEntityHandler {

    public MockDataPointEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

}
