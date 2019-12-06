package com.nubeiot.edge.module.datapoint.task.remote;

import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ErrorMessageConverter;
import com.nubeiot.core.micro.discovery.RemoteServiceInvoker;
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
        return fromCache(taskData).filter(pojo -> isNotRoundRobin(pojo, taskData.getOriginReqData()))
                                  .map(ProtocolDispatcher::getAddress)
                                  .flatMapSingleElement(address -> dispatch(address, taskData))
                                  .defaultIfEmpty(taskData.getData());
    }

    private boolean isNotRoundRobin(@NonNull ProtocolDispatcher pojo, @NonNull RequestData originReqData) {
        String requestBy = Optional.ofNullable(originReqData.headers())
                                   .map(h -> h.getString(Headers.X_REQUEST_BY))
                                   .orElse(null);
        return Strings.isNotBlank(pojo.getAddress()) &&
               !RemoteServiceInvoker.requestBy(pojo.getProtocol().type()).equals(requestBy);
    }

    private Single<VertxPojo> dispatch(@NonNull String address, @NonNull EntityTaskData<VertxPojo> taskData) {
        final RequestData reqData = RequestData.builder()
                                               .body(taskData.getData().toJson())
                                               .headers(taskData.getOriginReqData().headers())
                                               .build();
        final EventbusClient client = definition().handler().eventClient();
        final EventAction action = taskData.getOriginReqAction();
        final Single<EventMessage> invoker = new ProtocolDispatcherRpcClient(client).invoke(address, action, reqData);
        return invoker.flatMap(msg -> msg.isError()
                                      ? Single.error(ErrorMessageConverter.from(msg.getError()))
                                      : Single.just(Optional.ofNullable(msg.getData()).orElse(new JsonObject())))
                      .map(taskData.getMetadata()::parseFromRequest);
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
