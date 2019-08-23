package com.nubeiot.core.sql.validation;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

/**
 * Entity validation for simple case
 *
 * @param <P> Pojo type
 */
@SuppressWarnings("unchecked")
public interface EntityValidation<P extends VertxPojo> {

    String NOT_FOUND_MSG = "Not found resource with {0}";
    String ALREADY_EXISTED_MSG = "Already existed resource with {0}";
    String ALREADY_LINKED_MSG = "Resource with {0} is already referenced to resource with {1}";
    String RESOURCE_IS_USING_MSG = "Resource with {0} is using by another resource";

    EntityMetadata context();

    /**
     * Validate when creating new resource
     *
     * @param reqData given request resource object
     * @return instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull

    default <PP extends P> PP onCreating(RequestData reqData) throws IllegalArgumentException {
        return (PP) context().parseFromRequest(reqData.body());
    }

    /**
     * Validate when updating resource
     *
     * @param dbData  existing resource object from database
     * @param reqData given request resource object
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    default <PP extends P> PP onUpdating(@NonNull P dbData, RequestData reqData) throws IllegalArgumentException {
        final JsonObject body = reqData.body().copy();
        return (PP) context().parseFromRequest(body.put(context().jsonKeyName(), context().parseKey(reqData)));
    }

    /**
     * Validate when patching resource
     *
     * @param dbData  existing resource object from database
     * @param reqData given request resource object
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     */
    @NonNull
    default <PP extends P> PP onPatching(@NonNull P dbData, RequestData reqData) throws IllegalArgumentException {
        final JsonObject body = reqData.body().copy();
        body.put(context().jsonKeyName(), body.remove(context().requestKeyName()));
        return (PP) context().parseFromRequest(JsonPojo.merge(dbData, body));
    }

    default Object onDeleting(@NonNull RequestData reqData) throws IllegalArgumentException {
        return context().parseKey(reqData);
    }

    /**
     * Construct {@code NotFound exception} by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return NotFoundException
     */
    default NotFoundException notFound(@NonNull Object primaryKey) {
        return notFound(Strings.kvMsg(context().requestKeyName(), primaryKey));
    }

    /**
     * Construct {@code NotFound exception} by {@code entity key}
     *
     * @param pojoKey Given pojo key value
     * @return NotFoundException
     */
    default NotFoundException notFound(@NonNull String pojoKey) {
        return new NotFoundException(Strings.format(NOT_FOUND_MSG, pojoKey));
    }

    /**
     * Construct {@code AlreadyExist exception} by {@code entity key}
     *
     * @param pojoKey Given primary key
     * @return AlreadyExistException
     */
    default AlreadyExistException alreadyExisted(String pojoKey) {
        return new AlreadyExistException(Strings.format(ALREADY_EXISTED_MSG, pojoKey));
    }

    /**
     * Construct {@code AlreadyExistException exception} by {@code entity key}
     *
     * @param pojoKey Given pojo key value
     * @return NotFoundException
     */
    default AlreadyExistException unableDeleteDueUsing(String pojoKey) {
        return new AlreadyExistException(Strings.format(RESOURCE_IS_USING_MSG, pojoKey));
    }

}
