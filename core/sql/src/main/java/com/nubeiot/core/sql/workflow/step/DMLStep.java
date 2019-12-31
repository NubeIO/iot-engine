package com.nubeiot.core.sql.workflow.step;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.NonNull;

/**
 * Represents a {@code DML} step for single resource
 *
 * @since 1.0.0
 */
public interface DMLStep extends SQLStep {

    /**
     * Execute {@code SQL manipulate command} based on given {@code request data} and {@code validator}.
     *
     * @param reqData   the req data
     * @param validator the validator
     * @return DML pojo in Single
     * @see RequestData
     * @see OperationValidator
     * @see DMLPojo
     * @since 1.0.0
     */
    Single<DMLPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator);

    /**
     * Represents a {@code create} or {@code update} step
     *
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    interface CreateOrUpdateStep extends DMLStep {

        /**
         * Lookup created or modified entity by primary key
         *
         * @param dmlPojo Request pojo with primary key
         * @return wrapper pojo
         * @see DMLPojo
         * @since 1.0.0
         */
        default Single<DMLPojo> lookup(@NonNull DMLPojo dmlPojo) {
            return queryExecutor().lookupByPrimaryKey(dmlPojo.primaryKey())
                                  .map(p -> DMLPojo.builder()
                                                   .request(dmlPojo.request())
                                                   .primaryKey(dmlPojo.primaryKey())
                                                   .dbEntity((VertxPojo) p)
                                                   .build());
        }

    }

}
