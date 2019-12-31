package com.nubeiot.core.sql.workflow.step;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
public final class GetOneStep<T extends VertxPojo> extends AbstractSQLStep implements DQLStep<T> {

    @Override
    public Single<T> query(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        return queryExecutor().findOneByKey(reqData).flatMap(p -> validator.validate(reqData, (VertxPojo) p));
    }

}
