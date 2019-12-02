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
 */
public interface SQLStep extends Workflow {

    @NonNull EventAction action();

    @NonNull EntityQueryExecutor queryExecutor();

    /**
     * Represents a {@code DML} step
     */
    interface DMLStep extends SQLStep {

        Single<KeyPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator);

    }


    /**
     * Represents a {@code DQL} step
     */
    interface DQLStep<T> extends SQLStep {

        Single<T> query(@NonNull RequestData reqData, @NonNull OperationValidator validator);

    }


    /**
     * Represents a {@code create} or {@code update} step
     */
    @SuppressWarnings("unchecked")
    interface CreateOrUpdateStep extends DMLStep {

        /**
         * Lookup created or modified entity by primary key
         *
         * @param request    Request pojo
         * @param primaryKey primary key
         * @return wrapper pojo
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
