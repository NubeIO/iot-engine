package com.nubeiot.core.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;

import lombok.NonNull;

public interface ModelService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    extends EventListener {

    @NonNull
    default Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH, EventAction.REMOVE,
                             EventAction.GET_ONE, EventAction.GET_LIST);
    }

    @NonNull D getDao();

    @NonNull JsonTable<R> table();

    /**
     * Defines primary key name in json to lookup in {@code get/update/patch/delete} resource
     *
     * @return primary key name
     */
    @NonNull
    default String primaryKeyName() {
        return "id";
    }

    /**
     * Parse request data key to actual data type to look up in {@code get/update/patch/delete} resource.
     *
     * @param dataKey Request data key
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid
     */
    K parsePK(String dataKey) throws IllegalArgumentException;

    interface UUIDKeyModel<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, UUID>>
        extends ModelService<UUID, M, R, D> {

        default UUID parsePK(String dataKey) throws IllegalArgumentException {
            return UUID.fromString(dataKey);
        }

    }


    interface SerialKeyModel<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, Integer>>
        extends ModelService<Integer, M, R, D> {

        default Integer parsePK(String dataKey) throws IllegalArgumentException {
            return Integer.parseInt(dataKey);
        }

    }


    interface BigSerialKeyModel<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, Long>>
        extends ModelService<Long, M, R, D> {

        default Long parsePK(String dataKey) throws IllegalArgumentException {
            return Long.parseLong(dataKey);
        }

    }

}
