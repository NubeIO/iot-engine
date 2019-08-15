package com.nubeiot.core.sql;

import java.util.Locale;
import java.util.UUID;

import org.jooq.Table;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Entity metadata Holder
 *
 * @param <K> Entity Primary Key type
 * @param <P> Pojo entity type
 * @param <R> Record type
 * @param <D> DAO type
 */
public interface EntityMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>> {

    static <POJO extends VertxPojo> String createRequestKeyName(@NonNull Class<POJO> modelClass, String jsonKeyName) {
        return (Strings.toSnakeCaseLC(modelClass.getSimpleName()) + "_" +
                Strings.requireNotBlank(jsonKeyName)).toLowerCase(Locale.ENGLISH);
    }

    static <RECORD extends UpdatableRecord<RECORD>> String createJsonKeyName(@NonNull Table<RECORD> table) {
        if (table.getPrimaryKey().getFields().size() != 1) {
            throw new UnsupportedOperationException("Doesn't support composite key or no primary key");
        }
        return table.getPrimaryKey().getFields().iterator().next().getName().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Pojo model class
     *
     * @return model class
     */
    @NonNull Class<? extends P> modelClass();

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
     * Parse request data key to actual data type to look up in {@code get/update/patch/delete} resource.
     *
     * @param dataKey Request data key
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid or missing
     */
    @NonNull K parsePrimaryKey(@NonNull String dataKey) throws IllegalArgumentException;

    /**
     * Defines request key name that represents for {@code primary key} in {@code table} to lookup in doing {@code get
     * /update /patch /delete} resource
     *
     * @return request key name. Default is {@code <model_name>_<json_key_name>}
     * @apiNote It is not represents for actual primary key column in database table
     * @see #modelClass()
     */
    @NonNull
    default String requestKeyName() {
        return createRequestKeyName(modelClass(), jsonKeyName());
    }

    /**
     * Primary key column is represented in {@code json} mode
     * <p>
     * Default is primary key column in lowercase
     *
     * @return primary key column in json
     * @apiNote It doesn't support composite key or no primary key in {@code table}
     * @see #table()
     */
    @NonNull
    default String jsonKeyName() {
        return createJsonKeyName(table());
    }

    /**
     * Define response key name for single resource
     *
     * @return response key name
     * @apiNote Default is {@link #table()} name in lowercase
     */
    default @NonNull String singularKeyName() {
        return table().getName().toLowerCase();
    }

    /**
     * Defines response  key name for multiple resource
     *
     * @return response key name
     * @apiNote Default is {@link #singularKeyName()} appends "{@code s}" character
     */
    default @NonNull String pluralKeyName() {
        return singularKeyName() + "s";
    }

    /**
     * Represents entity primary key is in {@code Integer} data type
     *
     * @see EntityService
     */
    interface SerialKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, Integer>>
        extends EntityMetadata<Integer, M, R, D> {

        default Integer parsePrimaryKey(String dataKey) throws IllegalArgumentException {
            return Functions.toInt().apply(dataKey);
        }

    }


    /**
     * Represents entity primary key is in {@code Long} data type
     *
     * @see EntityService
     */
    interface BigSerialKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, Long>>
        extends EntityMetadata<Long, M, R, D> {

        default Long parsePrimaryKey(String dataKey) throws IllegalArgumentException {
            return Functions.toLong().apply(dataKey);
        }

    }


    /**
     * Represents entity primary key is in {@code String} data type
     *
     * @see EntityService
     */
    interface StringKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, String>>
        extends EntityMetadata<String, M, R, D> {

        default String parsePrimaryKey(String dataKey) throws IllegalArgumentException {
            return dataKey;
        }

    }


    /**
     * Represents entity primary key is in {@code UUID} data type
     *
     * @see EntityService
     */
    interface UUIDKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, UUID>>
        extends EntityMetadata<UUID, M, R, D> {

        default UUID parsePrimaryKey(String dataKey) throws IllegalArgumentException {
            return Functions.toUUID().apply(dataKey);
        }

    }

}
