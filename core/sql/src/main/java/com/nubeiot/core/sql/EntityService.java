package com.nubeiot.core.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.jooq.Table;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

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

    static <POJO extends VertxPojo> String createRequestKeyName(@NonNull Class<POJO> modelClass, String jsonKeyName) {
        return modelClass.getSimpleName().toLowerCase(Locale.ENGLISH) + "_" + Strings.requireNotBlank(jsonKeyName);
    }

    /**
     * Defines {@code CURD} actions
     *
     * @return set of default CRUD action
     */
    @NonNull
    default Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.UPDATE, EventAction.PATCH, EventAction.REMOVE,
                             EventAction.GET_ONE, EventAction.GET_LIST);
    }

    /**
     * Defines enabling {@code time audit} in {@code application layer} instead of {@code database layer} by {@code DB
     * trigger}. It is helpful to add time audit in {@code create/update/patch} resource.
     *
     * @return {@code true} if enable time audit in application layer
     * @see TimeAudit
     */
    boolean enableTimeAudit();

    /**
     * Enable {@code CUD} response includes full resource instead of simple resource with only response status and
     * {@code primary key} of resource.
     *
     * @return {@code true} if enable full resource in response
     */
    boolean enableFullResourceInCUDResponse();

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
     * Defines listener for listing Resource
     *
     * @param requestData Request data
     * @return Json object includes list data
     * @see EventAction#GET_LIST
     */
    Single<JsonObject> list(RequestData requestData);

    /**
     * Defines listener for get one item by key
     *
     * @param requestData Request data
     * @return Json object represents resource data
     * @see EventAction#GET_ONE
     */
    Single<JsonObject> get(RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     */
    Single<JsonObject> create(RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     */
    Single<JsonObject> update(RequestData requestData);

    /**
     * Defines listener for patching existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#PATCH
     */
    Single<JsonObject> patch(RequestData requestData);

    /**
     * Defines listener for deleting existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#REMOVE
     */
    Single<JsonObject> delete(RequestData requestData);

    /**
     * Parse given data from external service to {@code pojo} object
     *
     * @param request Given request data
     * @return {@code pojo} object resource
     * @throws IllegalArgumentException if cannot parse
     */
    @SuppressWarnings("unchecked")
    @NonNull
    default M parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return (M) ReflectionClass.createObject(modelClass()).fromJson(request);
    }

    /**
     * Defines request key name that represents for {@code primary key} in {@code table} to lookup in doing {@code get
     * /update /patch /delete} resource
     *
     * @return request key name. Default is {@code <model_name>_<json_key_name>}
     * @apiNote It is not represents for actual primary key column in database table
     * @see #parsePrimaryKey(RequestData)
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
        if (table().getPrimaryKey().getFields().size() != 1) {
            throw new UnsupportedOperationException("Doesn't support composite key or no primary key");
        }
        return table().getPrimaryKey().getFields().iterator().next().getName().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Extract primary key from request then parse to primary key with proper data type
     *
     * @param requestData Request data
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid or missing
     */
    @NonNull
    default K parsePrimaryKey(@NonNull RequestData requestData) throws IllegalArgumentException {
        return Optional.ofNullable(requestData.body().getValue(requestKeyName()))
                       .map(k -> parsePrimaryKey(k.toString()))
                       .orElseThrow(() -> new IllegalArgumentException("Missing key " + requestKeyName()));
    }

    /**
     * Parse request data key to actual data type to look up in {@code get/update/patch/delete} resource.
     *
     * @param dataKey Request data key
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid or missing
     */
    @NonNull K parsePrimaryKey(@NonNull String dataKey) throws IllegalArgumentException;

    /**
     * Represents entity primary key is in {@code UUID} data type
     *
     * @see EntityService
     */
    interface UUIDKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, UUID>>
        extends EntityService<UUID, M, R, D> {

        default UUID parsePrimaryKey(String dataKey) throws IllegalArgumentException {
            return Functions.toUUID().apply(dataKey);
        }

    }


    /**
     * Represents entity primary key is in {@code Integer} data type
     *
     * @see EntityService
     */
    interface SerialKeyEntity<M extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, M, Integer>>
        extends EntityService<Integer, M, R, D> {

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
        extends EntityService<Long, M, R, D> {

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
        extends EntityService<String, M, R, D> {

        default String parsePrimaryKey(String dataKey) throws IllegalArgumentException {
            return dataKey;
        }

    }

}
