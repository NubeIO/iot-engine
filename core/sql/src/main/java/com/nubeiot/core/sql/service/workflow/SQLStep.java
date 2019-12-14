package com.nubeiot.core.sql.service.workflow;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.KeyPojo;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.workflow.Workflow;

import lombok.NonNull;

/**
 * Represents a direct execution step into database
 *
 * @since 1.0.0
 */
public interface SQLStep extends Workflow {

    /**
     * Declares event action.
     *
     * @return the event action
     * @since 1.0.0
     */
    @NonNull EventAction action();

    /**
     * Declares entity query executor.
     *
     * @return the entity query executor
     * @see EntityQueryExecutor
     * @since 1.0.0
     */
    @NonNull EntityQueryExecutor queryExecutor();

    /**
     * Represents a {@code DML} step
     *
     * @since 1.0.0
     */
    interface DMLStep extends SQLStep {

        /**
         * Execute single.
         *
         * @param reqData   the req data
         * @param validator the validator
         * @return the single
         * @since 1.0.0
         */
        Single<KeyPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator);

    }


    /**
     * Represents a {@code DQL} step
     *
     * @param <T> Type of {@code parameter}
     * @since 1.0.0
     */
    interface DQLStep<T> extends SQLStep {

        /**
         * Query single.
         *
         * @param reqData   the req data
         * @param validator the validator
         * @return the single
         * @since 1.0.0
         */
        Single<T> query(@NonNull RequestData reqData, @NonNull OperationValidator validator);

    }


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
         * @param request    Request pojo
         * @param primaryKey primary key
         * @return wrapper pojo
         * @since 1.0.0
         */
        default Single<KeyPojo> lookup(VertxPojo request, @NonNull Object primaryKey) {
            return queryExecutor().lookupByPrimaryKey(primaryKey)
                                  .map(pojo -> KeyPojo.builder()
                                                      .request(request)
                                                      .key(primaryKey)
                                                      .pojo((VertxPojo) pojo)
                                                      .build());
        }

    }

}
