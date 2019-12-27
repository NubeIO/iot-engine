package com.nubeiot.core.sql.service.workflow;

import java.util.Objects;
import java.util.function.BiConsumer;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.service.workflow.SQLStep.DMLStep;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
public final class DeletionStep extends AbstractSQLStep implements DMLStep {

    @Setter
    private BiConsumer<EventAction, DMLPojo> onSuccess;

    @Override
    public Single<DMLPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Single<DMLPojo> result = queryExecutor().deleteOneByKey(reqData, validator)
                                                      .map(p -> DMLPojo.builder().dbEntity((VertxPojo) p).build());
        if (Objects.nonNull(onSuccess)) {
            return result.doOnSuccess(keyPojo -> onSuccess.accept(action(), keyPojo));
        }
        return result;
    }

}
