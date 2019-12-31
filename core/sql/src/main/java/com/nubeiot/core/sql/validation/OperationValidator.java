package com.nubeiot.core.sql.validation;

import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;

import lombok.NonNull;

/**
 * Represents for {@code DML} or {@code DQL} validator before do execute {@code SQL operation} in database
 * <p>
 * It can be used to validate request data or check entity permission
 *
 * @since 1.0.0
 */
public interface OperationValidator {

    /**
     * Create operation validator.
     *
     * @param validation the validation function
     * @return the operation validator
     * @since 1.0.0
     */
    @NonNull
    static OperationValidator create(@NonNull BiFunction<RequestData, VertxPojo, Single<VertxPojo>> validation) {
        return new DefaultOperationValidator(validation);
    }

    /**
     * Validate entity from request data
     *
     * @param reqData  request data
     * @param dbEntity previous entity. It can be {@code null} in case of {@code Create}
     * @return entity after validate
     * @since 1.0.0
     */
    @NonNull Single<VertxPojo> validate(@NonNull RequestData reqData, VertxPojo dbEntity);

    /**
     * Defines action after validating.
     * <p>
     * It can be used to inject an extra validator such as the permission validation on each record step, etc
     *
     * @param andThen extra validator
     * @return a reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    @NonNull OperationValidator andThen(OperationValidator andThen);

}
