package com.nubeiot.edge.module.datapoint.task.remote;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.task.EntityTask;
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.edge.module.datapoint.service.ProtocolDispatcherService;
import com.nubeiot.iotdata.dto.Protocol;

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
        final DeliveryEvent event = createProtocolDispatcherEvent(executionData);
        final EventController eventClient = definition().handler().eventClient();
        eventClient.request(event, ReplyEventHandler.builder().build());
        return Single.just(false);
    }

    @Override
    public @NonNull Maybe<VertxPojo> execute(@NonNull EntityTaskData<VertxPojo> executionData) {
        return Maybe.empty();
    }

    private DeliveryEvent createProtocolDispatcherEvent(@NonNull EntityTaskData<VertxPojo> executionData) {
        final String entity = executionData.getMetadata().singularKeyName();
        final JsonObject filter = new JsonObject().put(ProtocolDispatcherMetadata.INSTANCE.table().ENTITY.getName(),
                                                       entity)
                                                  .put(ProtocolDispatcherMetadata.INSTANCE.table().PROTOCOL.getName(),
                                                       Protocol.BACNET);
        return DeliveryEvent.builder()
                            .address(ProtocolDispatcherService.class.getName())
                            .action(EventAction.GET_LIST)
                            .addPayload(RequestData.builder().filter(filter).build())
                            .build();
    }

}
