package com.nubeiot.edge.connector.datapoint.model;

import java.util.function.Supplier;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

public interface IDittoModel<V extends VertxPojo> extends Supplier<V> {

    String POINT_JQ_EXPR = ".thing.features.points.properties";

    static <D extends IDittoModel> D mock(@NonNull Class<D> clazz) {
        return ReflectionClass.createObject(clazz);
    }

    String jqExpr();

}
