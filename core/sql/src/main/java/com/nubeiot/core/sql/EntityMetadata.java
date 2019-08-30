package com.nubeiot.core.sql;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UpdatableRecord;

import io.github.jklingsporn.vertx.jooq.rx.VertxDAO;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.service.EntityService;
import com.nubeiot.core.sql.tables.JsonTable;
import com.nubeiot.core.sql.type.TimeAudit;
import com.nubeiot.core.sql.validation.EntityValidation;
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
 * @see EntityService
 */
public interface EntityMetadata<K, P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, K>>
    extends EntityValidation<P> {

    static <POJO extends VertxPojo> String createRequestKeyName(@NonNull Class<POJO> modelClass, String jsonKeyName) {
        return (Strings.toSnakeCaseLC(modelClass.getSimpleName()) + "_" +
                Strings.requireNotBlank(jsonKeyName)).toLowerCase(Locale.ENGLISH);
    }

    static String createJsonKeyName(@NonNull Table<? extends Record> table) {
        if (table.getPrimaryKey().getFields().size() != 1) {
            throw new UnsupportedOperationException("Doesn't support composite key or no primary key");
        }
        return table.getPrimaryKey().getFields().iterator().next().getName().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Defines enabling {@code time audit} in {@code application layer} instead of {@code database layer} by {@code DB
     * trigger}. It is helpful to add time audit in {@code create/update/patch} resource.
     *
     * @return {@code true} if enable time audit in application layer
     * @see TimeAudit
     */
    default boolean enableTimeAudit() {
        return true;
    }

    /**
     * Declare entity table
     *
     * @return entity table
     * @see Table
     * @see JsonTable
     */
    @NonNull JsonTable<R> table();

    /**
     * Pojo model class
     *
     * @return model class
     */
    @NonNull <PP extends P> Class<PP> modelClass();

    /**
     * DAO class
     *
     * @return dao class
     */
    @NonNull Class<D> daoClass();

    /**
     * Get DAO
     *
     * @param handler Entity handler
     * @return DAO that corresponding to {@link #daoClass()}
     */
    default @NonNull D dao(EntityHandler handler) {
        return handler.dao(daoClass());
    }

    @Override
    default EntityMetadata context() { return this; }

    /**
     * Parse given data from entity database to {@code pojo} object
     *
     * @param request Given entity data
     * @return {@code pojo} object resource
     * @throws IllegalArgumentException if cannot parse
     */
    @NonNull
    @SuppressWarnings("unchecked")
    default <PP extends P> PP parseFromEntity(@NonNull JsonObject request) throws IllegalArgumentException {
        return (PP) EntityHandler.parse(modelClass(), request);
    }

    /**
     * Parse given data from service request to {@code pojo} object
     *
     * @param request Given request data
     * @return {@code pojo} object resource
     * @throws IllegalArgumentException if cannot parse
     */
    @NonNull
    default <PP extends P> PP parseFromRequest(@NonNull JsonObject request) throws IllegalArgumentException {
        return parseFromEntity(request);
    }

    @NonNull
    default Optional<K> getKey(@NonNull RequestData requestData) throws IllegalArgumentException {
        return Optional.ofNullable(requestData.body())
                       .flatMap(body -> Optional.ofNullable(body.getValue(requestKeyName())))
                       .map(Object::toString)
                       .map(this::parseKey);
    }

    /**
     * Extract {@code primary/unique key} from request by {@link #requestKeyName()} then parse to proper data type
     *
     * @param requestData Request data
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid or missing
     * @see #requestKeyName()
     * @see #parseKey(String)
     */
    @NonNull
    default K parseKey(@NonNull RequestData requestData) throws IllegalArgumentException {
        return getKey(requestData).orElseThrow(() -> new IllegalArgumentException("Missing key " + requestKeyName()));
    }

    /**
     * Parse request data key to actual data type to look up in {@code get/update/patch/delete} resource.
     *
     * @param dataKey Request data key
     * @return Actual primary key
     * @throws IllegalArgumentException if data key is not valid or missing
     */
    @NonNull K parseKey(@NonNull String dataKey) throws IllegalArgumentException;

    /**
     * Defines request key name that represents for {@code primary/unique key} in {@code table} to lookup in doing
     * {@code get /update /patch /delete} resource
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
     * @see EntityMetadata
     */
    interface SerialKeyEntity<P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, Integer>>
        extends EntityMetadata<Integer, P, R, D> {

        default Integer parseKey(String dataKey) throws IllegalArgumentException {
            return Functions.toInt().apply(dataKey);
        }

    }


    /**
     * Represents entity primary key is in {@code Long} data type
     *
     * @see EntityMetadata
     */
    interface BigSerialKeyEntity<P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, Long>>
        extends EntityMetadata<Long, P, R, D> {

        default Long parseKey(String dataKey) throws IllegalArgumentException {
            return Functions.toLong().apply(dataKey);
        }

    }


    /**
     * Represents entity primary key is in {@code String} data type
     *
     * @see EntityMetadata
     */
    interface StringKeyEntity<P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, String>>
        extends EntityMetadata<String, P, R, D> {

        default String parseKey(String dataKey) throws IllegalArgumentException {
            return dataKey;
        }

    }


    /**
     * Represents entity primary key is in {@code UUID} data type
     *
     * @see EntityMetadata
     */
    interface UUIDKeyEntity<P extends VertxPojo, R extends UpdatableRecord<R>, D extends VertxDAO<R, P, UUID>>
        extends EntityMetadata<UUID, P, R, D> {

        default UUID parseKey(String dataKey) throws IllegalArgumentException {
            return Functions.getOrThrow(() -> Functions.toUUID().apply(dataKey),
                                        () -> new IllegalArgumentException("Invalid key"));
        }

    }

}
