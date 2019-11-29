package com.nubeiot.core.sql.service.workflow;

import java.util.function.BiConsumer;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.KeyPojo;
import com.nubeiot.core.sql.query.EntityQueryExecutor;
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
public final class DeletionStep implements PersistStep {

    @Default
    private final EventAction action = EventAction.REMOVE;
    private final EntityQueryExecutor queryExecutor;
    @Setter
    private BiConsumer<EventAction, KeyPojo> onSuccess;

    @Override
    public Single<KeyPojo> execute(@NonNull RequestData reqData, @NonNull OperationValidator validator) {
        return queryExecutor().deleteOneByKey(reqData, validator)
                              .map(p -> KeyPojo.builder().pojo((VertxPojo) p).build());
    }

}
