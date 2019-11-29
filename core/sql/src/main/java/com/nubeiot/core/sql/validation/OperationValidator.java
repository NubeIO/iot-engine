package com.nubeiot.core.sql.validation;

import java.util.function.BiFunction;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;

import lombok.NonNull;

public interface OperationValidator {

    static OperationValidator create(BiFunction<RequestData, VertxPojo, Single<VertxPojo>> validate) {
        return DefaultOperationValidator.builder().validate(validate).build();
    }

    /**
     * Validate entity from request data
     *
     * @param reqData  request data
     * @param dbEntity previous entity. It can be {@code null} in case of {@code Create} or {@code Remove}
     * @return entity after validate
     */
    Single<VertxPojo> validate(@NonNull RequestData reqData, VertxPojo dbEntity);

    OperationValidator andThen(BiFunction<RequestData, VertxPojo, Single<VertxPojo>> andThen);

}
