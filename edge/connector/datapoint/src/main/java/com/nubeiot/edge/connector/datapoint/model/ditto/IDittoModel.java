package com.nubeiot.edge.connector.datapoint.model.ditto;

import java.util.function.Supplier;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Migrate/convert data between {@code Ditto/LowDB} and {@code Postgres/H2}
 *
 * @param <V> Pojo class that represents for data entity
 */
public interface IDittoModel<V extends VertxPojo> extends JsonData, Supplier<V> {

    /**
     * Ditto endpoint
     *
     * @param thingId Thing Id
     * @return Ditto URL endpoint
     */
    String endpoint(String thingId);

    @RequiredArgsConstructor
    abstract class AbstractDittoModel<V extends VertxPojo> implements IDittoModel<V> {

        @NonNull
        private final V data;

        @Override
        public final V get() {
            return data;
        }

        @Override
        public JsonObject toJson() {
            return null;
        }

    }

}
