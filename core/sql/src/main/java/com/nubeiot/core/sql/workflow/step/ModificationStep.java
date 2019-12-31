package com.nubeiot.core.sql.workflow.step;

import java.util.Objects;

import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.DMLPojo;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.sql.workflow.step.DMLStep.CreateOrUpdateStep;
import com.nubeiot.core.utils.Functions.TripleConsumer;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
public final class ModificationStep extends AbstractSQLStep implements CreateOrUpdateStep {

    @Setter
    private TripleConsumer<RequestData, EventAction, DMLPojo> onSuccess;

    @Override
    public Single<DMLPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Single<DMLPojo> result = queryExecutor().modifyReturningPrimary(reqData, validator)
                                                      .flatMap(dmlPojo -> lookup((DMLPojo) dmlPojo));
        if (Objects.nonNull(onSuccess)) {
            return result.doOnSuccess(keyPojo -> onSuccess.accept(reqData, action(), keyPojo));
        }
        return result;
    }

}
