package com.nubeiot.core.sql.validation;

import java.util.Optional;

import org.jooq.Field;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.BeingUsedException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.JsonUtils;

import lombok.NonNull;

/**
 * Entity validation for request input
 *
 * @param <P> Type of {@code VertxPojo}
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public interface EntityValidation<P extends VertxPojo> {

    /**
     * The constant NOT_FOUND_MSG.
     */
    String NOT_FOUND_MSG = "Not found resource with {0}";
    /**
     * The constant ALREADY_EXISTED_MSG.
     */
    String ALREADY_EXISTED_MSG = "Already existed resource with {0}";
    /**
     * The constant RESOURCE_IS_USING_MSG.
     */
    String RESOURCE_IS_USING_MSG = "Resource with {0} is using by another resource";
    /**
     * The constant MANDATORY_MSG.
     */
    String MANDATORY_MSG = "{0} is mandatory";

    /**
     * Context entity metadata.
     *
     * @return the entity metadata
     * @see EntityMetadata
     * @since 1.0.0
     */
    EntityMetadata context();

    /**
     * Validate when creating new resource
     *
     * @param <PP>    Type of {@code VertxPojo}
     * @param reqData given request resource object
     * @return instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     * @since 1.0.0
     */
    @NonNull
    default <PP extends P> PP onCreating(@NonNull RequestData reqData) throws IllegalArgumentException {
        return (PP) context().parseFromRequest(reqData.body());
    }

    /**
     * Validate when updating resource
     *
     * @param <PP>    Type of {@code VertxPojo}
     * @param dbData  existing resource object from database
     * @param reqData given request resource object
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     * @since 1.0.0
     */
    @NonNull
    default <PP extends P> PP onUpdating(@NonNull P dbData, @NonNull RequestData reqData)
        throws IllegalArgumentException {
        final JsonObject body = reqData.body().copy();
        final Object key = Optional.ofNullable(body.remove(context().requestKeyName()))
                                   .orElse(body.remove(context().jsonKeyName()));
        body.put(context().jsonKeyName(), JsonData.checkAndConvert(context().parseKey(Strings.toString(key))));
        return (PP) context().parseFromRequest(body);
    }

    /**
     * Validate when patching resource
     *
     * @param <PP>    Type of {@code VertxPojo}
     * @param dbData  existing resource object from database
     * @param reqData given request resource object
     * @return pojo instance for fluent API
     * @throws IllegalArgumentException if any invalid parameter
     * @since 1.0.0
     */
    @NonNull
    default <PP extends P> PP onPatching(@NonNull P dbData, @NonNull RequestData reqData)
        throws IllegalArgumentException {
        final JsonObject body = reqData.body().copy();
        final Object key = Optional.ofNullable(body.remove(context().requestKeyName()))
                                   .orElse(body.remove(context().jsonKeyName()));
        body.put(context().jsonKeyName(), JsonData.checkAndConvert(context().parseKey(Strings.toString(key))));
        return (PP) context().parseFromRequest(JsonPojo.merge(dbData, body));
    }

    /**
     * Validate when deleting resource.
     *
     * @param <PP>    Type of {@code VertxPojo}
     * @param dbData  the db data
     * @param reqData the req data
     * @return the pp
     * @throws IllegalArgumentException the illegal argument exception
     * @since 1.0.0
     */
    default <PP extends P> PP onDeleting(@NonNull P dbData, @NonNull RequestData reqData)
        throws IllegalArgumentException {
        return (PP) dbData;
    }

    /**
     * Construct {@code NotFound exception} by {@code primary key}
     *
     * @param primaryKey Given primary key
     * @return not found exception
     * @see NotFoundException
     * @since 1.0.0
     */
    default NotFoundException notFound(@NonNull Object primaryKey) {
        return notFound(JsonUtils.kvMsg(context().requestKeyName(), primaryKey));
    }

    /**
     * Construct {@code NotFound exception} by {@code entity key}
     *
     * @param pojoKey Given pojo key value
     * @return not found exception
     * @see NotFoundException
     * @since 1.0.0
     */
    default NotFoundException notFound(@NonNull String pojoKey) {
        return new NotFoundException(Strings.format(NOT_FOUND_MSG, pojoKey));
    }

    /**
     * Construct {@code AlreadyExist exception} by {@code entity key}
     *
     * @param pojoKey Given primary key
     * @return already exist exception
     * @see AlreadyExistException
     * @since 1.0.0
     */
    default AlreadyExistException alreadyExisted(String pojoKey) {
        return new AlreadyExistException(Strings.format(ALREADY_EXISTED_MSG, pojoKey));
    }

    /**
     * Construct {@code BeingUsedException exception} by {@code entity key}
     *
     * @param pojoKey Given pojo key value
     * @return already exist exception
     * @see BeingUsedException
     * @since 1.0.0
     */
    default BeingUsedException unableDeleteDueUsing(String pojoKey) {
        return new BeingUsedException(Strings.format(RESOURCE_IS_USING_MSG, pojoKey));
    }

    /**
     * Construct {@code IllegalArgumentException exception} by mandatory field.
     *
     * @param field the table field
     * @return the illegal argument exception
     * @since 1.0.0
     */
    default IllegalArgumentException mandatoryField(@NonNull String field) {
        return new IllegalArgumentException(Strings.format(MANDATORY_MSG, field));
    }

    /**
     * Construct {@code IllegalArgumentException exception} by mandatory field.
     *
     * @param field the field
     * @return the illegal argument exception
     * @since 1.0.0
     */
    default IllegalArgumentException mandatoryField(@NonNull Field field) {
        return mandatoryField(context().table().getJsonField(field));
    }

}
