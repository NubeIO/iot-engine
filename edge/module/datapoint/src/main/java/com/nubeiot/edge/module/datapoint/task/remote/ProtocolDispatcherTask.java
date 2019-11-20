package com.nubeiot.edge.module.datapoint.task.remote;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.sql.service.task.EntityTask;
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.edge.module.datapoint.service.ProtocolDispatcherService;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.NonNull;

public final class ProtocolDispatcherTask implements EntityTask<ProtocolTaskContext, VertxPojo, VertxPojo> {

    private final ProtocolTaskContext taskContext;

    public ProtocolDispatcherTask(@NonNull EntityHandler entityHandler) {
        this.taskContext = new ProtocolTaskContext(entityHandler);
    }

    @Override
    public ProtocolTaskContext definition() {
        return taskContext;
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityTaskData<VertxPojo> executionData) {
        final EventMessage msg = createProtocolDispatcherMsg(executionData);
        final EventbusClient eventClient = definition().handler().eventClient();
        return eventClient.request(ProtocolDispatcherService.class.getName(), msg)
                          .map(EventMessage::getData)
                          .map(response -> response.getJsonArray(ProtocolDispatcherMetadata.INSTANCE.pluralKeyName()))
                          .map(out -> !out.isEmpty());
    }

    @Override
    public @NonNull Maybe<VertxPojo> execute(@NonNull EntityTaskData<VertxPojo> executionData) {
        return Maybe.empty();
    }

    private EventMessage createProtocolDispatcherMsg(@NonNull EntityTaskData<VertxPojo> taskData) {
        final ProtocolDispatcher pojo = new ProtocolDispatcher().setProtocol(Protocol.BACNET)
                                                                .setEntity(taskData.getMetadata().singularKeyName())
                                                                .setAction(taskData.getOriginReqAction());
        return EventMessage.initial(EventAction.GET_LIST,
                                    RequestData.builder().filter(JsonPojo.from(pojo).toJson()).build());
    }

}
