package com.nubeiot.core.sql.service.workflow;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.KeyPojo;
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.core.sql.validation.OperationValidator;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public final class DefaultDMLWorkflow implements DMLWorkflow {

    @NonNull
    private final EventAction action;
    @NonNull
    private final EntityMetadata metadata;
    @NonNull
    private final Function<RequestData, RequestData> normalize;
    @NonNull
    private final OperationValidator validator;
    @NonNull
    private final TaskWorkflow.BlockingTaskWorkflow prePersist;
    @NonNull
    private final PersistStep persist;
    @NonNull
    private final TaskWorkflow.AsyncTaskWorkflow postAsyncPersist;
    @NonNull
    private final BiFunction<RequestData, KeyPojo, Single<JsonObject>> transformer;

    @Override
    public Single<JsonObject> execute(@NonNull RequestData requestData) {
        final RequestData reqData = normalize().apply(requestData);
        return persist().execute(reqData, validator().andThen((req, p) -> prePersist().execute(success(req, p))))
                        .doOnSuccess(db -> postAsyncPersist().execute(success(reqData, db.pojo())))
                        .doOnError(err -> postAsyncPersist().execute(error(reqData, err)))
                        .flatMap(pojo -> transformer().apply(reqData, pojo));
    }

    private EntityTaskData<VertxPojo> success(@NonNull RequestData reqData, @NonNull VertxPojo pojo) {
        return taskData(reqData, pojo, null);
    }

    private EntityTaskData<VertxPojo> error(@NonNull RequestData reqData, @NonNull Throwable err) {
        return taskData(reqData, null, err);
    }

    private EntityTaskData<VertxPojo> taskData(@NonNull RequestData reqData, VertxPojo pojo, Throwable t) {
        return EntityTaskData.builder()
                             .originReqData(reqData)
                             .originReqAction(action())
                             .metadata(metadata())
                             .data(pojo)
                             .throwable(t)
                             .build();
    }

}
