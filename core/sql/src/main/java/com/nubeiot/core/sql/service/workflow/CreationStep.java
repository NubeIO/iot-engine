package com.nubeiot.core.sql.service.workflow;

import java.util.Objects;
import java.util.function.BiConsumer;

import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.KeyPojo;
import com.nubeiot.core.sql.service.workflow.SQLStep.CreateOrUpdateStep;
import com.nubeiot.core.sql.validation.OperationValidator;

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
    private BiConsumer<EventAction, KeyPojo> onSuccess;

    @Override
    public Single<KeyPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Single<KeyPojo> result = validator.validate(reqData, null)
                                                .flatMap(pojo -> queryExecutor().insertReturningPrimary(pojo, reqData)
                                                                                .flatMap(pk -> lookup(pojo, pk)));
        if (Objects.nonNull(onSuccess)) {
            return result.doOnSuccess(keyPojo -> onSuccess.accept(action(), keyPojo));
        }
        return result;
    }

}
