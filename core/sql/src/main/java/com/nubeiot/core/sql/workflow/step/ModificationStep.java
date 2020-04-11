package com.nubeiot.core.sql.workflow.step;

import java.util.Objects;

import org.jooq.Configuration;

import io.github.zero.utils.Functions.TripleConsumer;
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
@SuperBuilder
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
public final class ModificationStep extends AbstractSQLStep implements CreateOrUpdateStep {

    @Setter
    private TripleConsumer<RequestData, EventAction, DMLPojo> onSuccess;

    @Override
    public Single<DMLPojo> execute(@NonNull RequestData requestData, @NonNull OperationValidator validator,
                                   Configuration configuration) {
        final Single<DMLPojo> result = queryExecutor().runtimeConfiguration(configuration)
                                                      .modifyReturningPrimary(requestData, validator)
                                                      .flatMap(dmlPojo -> lookup((DMLPojo) dmlPojo));
        if (Objects.nonNull(onSuccess)) {
            return result.doOnSuccess(keyPojo -> onSuccess.accept(requestData, action(), keyPojo));
        }
        return result;
    }

}
