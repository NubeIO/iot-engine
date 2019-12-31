package com.nubeiot.core.sql.workflow.step;

import java.util.Objects;
import java.util.function.BiConsumer;

import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.DMLStep.CreateOrUpdateStep;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@Accessors(fluent = true)
@SuperBuilder
@SuppressWarnings("unchecked")
public final class CreationStep extends AbstractSQLStep implements CreateOrUpdateStep {

    @Setter
    private BiConsumer<EventAction, DMLPojo> onSuccess;

    @Override
    public Single<DMLPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Single<DMLPojo> result = queryExecutor().insertReturningPrimary(reqData, validator)
                                                      .flatMap(pojo -> lookup((DMLPojo) pojo));
        if (Objects.nonNull(onSuccess)) {
            return result.doOnSuccess(keyPojo -> onSuccess.accept(action(), keyPojo));
        }
        return result;
    }

}
