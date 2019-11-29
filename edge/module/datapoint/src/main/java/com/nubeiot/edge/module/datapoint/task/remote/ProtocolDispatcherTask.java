package com.nubeiot.edge.module.datapoint.task.remote;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.ServiceNotFoundException;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.task.EntityTask;
import com.nubeiot.core.sql.service.task.EntityTaskData;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;
import com.nubeiot.edge.module.datapoint.cache.ProtocolDispatcherCache;
import com.nubeiot.edge.module.datapoint.model.pojos.HasProtocol;
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
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityTaskData<VertxPojo> taskData) {
        return fromCache(taskData).isEmpty().map(b -> !b);
    }

    @Override
    public @NonNull Maybe<VertxPojo> execute(@NonNull EntityTaskData<VertxPojo> taskData) {
        return fromCache(taskData).map(ProtocolDispatcher::getAddress)
                                  .filter(Strings::isNotBlank)
                                  .flatMapSingleElement(address -> dispatch(address, taskData))
                                  .defaultIfEmpty(taskData.getData());
    }

    private Single<VertxPojo> dispatch(@NonNull String address, @NonNull EntityTaskData<VertxPojo> taskData) {
        final EventMessage req = EventMessage.initial(taskData.getOriginReqAction(),
                                                      RequestData.builder().body(taskData.getData().toJson()).build());
        return definition().handler().eventClient().request(address, req).onErrorReturn(error -> {
            throw new ServiceNotFoundException("Protocol service is out of service. Try again later", error);
        }).map(msg -> {
            if (msg.isError()) {
                final ErrorMessage errorMsg = msg.getError();
                throw new NubeException(errorMsg.getCode(), errorMsg.getMessage());
            }
            return msg.getData();
        }).map(taskData.getMetadata()::parseFromRequest);
    }

    @SuppressWarnings("unchecked")
    private Maybe<ProtocolDispatcher> fromCache(@NonNull EntityTaskData<VertxPojo> taskData) {
        final ProtocolDispatcherCache cache = definition().handler()
                                                          .sharedData(DataCacheInitializer.PROTOCOL_DISPATCHER_CACHE);
        final Protocol protocol = taskData.getMetadata() instanceof HasProtocol
                                  ? ((HasProtocol) taskData.getMetadata()).getProtocol(taskData.getData())
                                  : Protocol.UNKNOWN;
        final ProtocolDispatcher pojo = new ProtocolDispatcher().setProtocol(protocol)
                                                                .setEntity(taskData.getMetadata().singularKeyName())
                                                                .setAction(taskData.getOriginReqAction());
        return cache.get(ProtocolDispatcherCache.serializeKey(pojo));
    }

}
