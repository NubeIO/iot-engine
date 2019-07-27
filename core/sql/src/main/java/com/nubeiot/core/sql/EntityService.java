package com.nubeiot.core.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

import org.jooq.Table;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

/**
 * Event Database entity service
 *
 * @param <K> Entity Primary Key
 * @param <M> Pojo entity
 * @param <R> Record entity
 * @param <D> DAO entity
 * @see EventListener
 * @see UpdatableRecord
 * @see VertxPojo
 * @see VertxDAO
 */
public interface EntityService<K, M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, K>>
    extends EventListener, Supplier<D> {

    /**
     * Defines {@code CURD} actions
     *
     * @return set of default CRUD action
     */
    @NonNull
    default Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH, EventAction.REMOVE, EventAction.GET_ONE, EventAction.GET_LIST);
    }

    /**
     * Pojo model class
     *
     * @return model class
     */
    @NonNull Class<M> modelClass();

    /**
     * DAO class
     *
     * @return dao class
     */
    @NonNull Class<D> daoClass();

    /**
     * Declare entity table
     *
     * @return entity table
     * @see Table
     * @see JsonTable
     */
    @NonNull JsonTable<R> table();

    /**
     * Parse given data from external service to {@code pojo} object
     *
     * @param request Given request data
     * @return {@code pojo} object resource
     * @throws IllegalArgumentException if cannot parse
     */
    @SuppressWarnings("unchecked")
    default @NonNull M parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return (M) ReflectionClass.createObject(modelClass()).fromJson(request);
    }

    /**
     * Parse request data key to actual data type to look up in {@code get/update/patch/delete} resource.
     *
     * @param dataKey Request data key
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid
     */
    K parsePK(String dataKey) throws IllegalArgumentException;

    /**
     * Represents entity primary key is in {@code UUID} data type
     *
     * @see EntityService
     */
    interface UUIDKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, UUID>>
        extends EntityService<UUID, M, R, D> {

        default UUID parsePK(String dataKey) throws IllegalArgumentException {
            return UUID.fromString(dataKey);
        }

    }


    /**
     * Represents entity primary key is in {@code Integer} data type
     *
     * @see EntityService
     */
    interface SerialKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, Integer>>
        extends EntityService<Integer, M, R, D> {

        default Integer parsePK(String dataKey) throws IllegalArgumentException {
            return Integer.parseInt(dataKey);
        }

    }


    /**
     * Represents entity primary key is in {@code Long} data type
     *
     * @see EntityService
     */
    interface BigSerialKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, Long>>
        extends EntityService<Long, M, R, D> {

        default Long parsePK(String dataKey) throws IllegalArgumentException {
            return Long.parseLong(dataKey);
        }

    }

}
