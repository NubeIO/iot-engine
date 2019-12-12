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
     * @param validate the validate
     * @return the operation validator
     * @since 1.0.0
     */
    static OperationValidator create(BiFunction<RequestData, VertxPojo, Single<VertxPojo>> validate) {
        return DefaultOperationValidator.builder().validate(validate).build();
    }

    /**
     * Validate entity from request data
     *
     * @param reqData  request data
     * @param dbEntity previous entity. It can be {@code null} in case of {@code Create}
     * @return entity after validate
     * @since 1.0.0
     */
    Single<VertxPojo> validate(@NonNull RequestData reqData, VertxPojo dbEntity);

    /**
     * Defines action after validating
     *
     * @param andThen post validation action
     * @return a reference to this, so the API can be used fluently
     * @since 1.0.0
     */
    OperationValidator andThen(BiFunction<RequestData, VertxPojo, Single<VertxPojo>> andThen);

}
