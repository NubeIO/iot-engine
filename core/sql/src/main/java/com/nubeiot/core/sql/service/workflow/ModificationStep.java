package com.nubeiot.core.sql.service.workflow;

import java.util.Objects;

import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.KeyPojo;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
import com.nubeiot.core.sql.service.workflow.PersistStep.CreateOrUpdateStep;
import com.nubeiot.core.sql.validation.OperationValidator;
import com.nubeiot.core.utils.Functions.TripleConsumer;

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
public final class ModificationStep implements CreateOrUpdateStep {

    @Default
    private final EventAction action;
    private final EntityQueryExecutor queryExecutor;
    @Setter
    private TripleConsumer<RequestData, EventAction, KeyPojo> onSuccess;

    @Override
    public Single<KeyPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        final Single<KeyPojo> result = queryExecutor().modifyReturningPrimary(reqData, action, validator)
                                                      .flatMap(pk -> lookup(null, pk));
        if (Objects.nonNull(onSuccess)) {
            return result.doOnSuccess(keyPojo -> onSuccess.accept(reqData, action, keyPojo));
        }
        return result;
    }

}
