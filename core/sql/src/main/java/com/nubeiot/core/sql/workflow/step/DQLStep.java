package com.nubeiot.core.sql.workflow.step;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Represents a {@code DQL} step
 *
 * @param <T> Type of {@code VertxPojo}
 * @since 1.0.0
 */
public interface DQLStep<T extends VertxPojo> extends SQLStep {

    /**
     * Do {@code SQL Query} based on given {@code request data} and {@code validator}.
     *
     * @param reqData   the req data
     * @param validator the validator
     * @return result in Single
     * @see RequestData
     * @see OperationValidator
     * @since 1.0.0
     */
    Single<T> query(@NonNull RequestData reqData, @NonNull OperationValidator validator);

}
