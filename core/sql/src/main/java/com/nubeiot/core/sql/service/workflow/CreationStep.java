package com.nubeiot.core.sql.service.workflow;

import java.util.Objects;
import java.util.function.BiConsumer;

import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.KeyPojo;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.service.workflow.PersistStep.CreateOrUpdateStep;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
@SuppressWarnings("unchecked")
public final class CreationStep implements CreateOrUpdateStep {

    @Default
    private final EventAction action = EventAction.CREATE;
    private final EntityQueryExecutor queryExecutor;
    @Setter
    private BiConsumer<EventAction, KeyPojo> onSuccess;

    @Override
    public Single<KeyPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Single<KeyPojo> result = validator.validate(reqData, null)
                                                .flatMap(req -> queryExecutor().insertReturningPrimary(req, reqData)
                                                                               .flatMap(pk -> lookup(req, pk)));
        if (Objects.nonNull(onSuccess)) {
            return result.doOnSuccess(keyPojo -> onSuccess.accept(action, keyPojo));
        }
        return result;
    }

}
