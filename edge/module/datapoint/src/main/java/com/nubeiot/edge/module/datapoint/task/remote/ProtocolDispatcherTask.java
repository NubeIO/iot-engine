package com.nubeiot.edge.module.datapoint.task.remote;

import java.util.Optional;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.github.zero.utils.Strings;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.DataTransferObject.Headers;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.ErrorMessageConverter;
import com.nubeiot.core.micro.discovery.RemoteServiceInvoker;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.workflow.task.EntityDefinitionContext;
import com.nubeiot.core.sql.workflow.task.EntityRuntimeContext;
import com.nubeiot.core.sql.workflow.task.ProxyEntityTask;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;
import com.nubeiot.edge.module.datapoint.cache.ProtocolDispatcherCache;
import com.nubeiot.edge.module.datapoint.model.pojos.HasProtocol;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class ProtocolDispatcherTask
    implements ProxyEntityTask<EntityDefinitionContext, VertxPojo, VertxPojo, EventbusClient> {

    @Getter
    private final EntityDefinitionContext definitionContext;

    public ProtocolDispatcherTask(@NonNull EntityHandler entityHandler) {
        this.definitionContext = EntityDefinitionContext.create(entityHandler);
    }

    @Override
    public EventbusClient transporter() {
        return definitionContext().entityHandler().eventClient();
    }

    @Override
    public @NonNull Single<Boolean> isExecutable(@NonNull EntityRuntimeContext<VertxPojo> runtimeContext) {
        return fromCache(runtimeContext).isEmpty().map(b -> !b);
    }

    @Override
    public @NonNull Maybe<VertxPojo> execute(@NonNull EntityRuntimeContext<VertxPojo> runtimeContext) {
        return fromCache(runtimeContext).filter(pojo -> isNotRoundRobin(pojo, runtimeContext.getOriginReqData()))
                                        .map(ProtocolDispatcher::getAddress)
                                        .flatMapSingleElement(address -> dispatch(address, runtimeContext))
                                        .defaultIfEmpty(runtimeContext.getData());
    }

    private boolean isNotRoundRobin(@NonNull ProtocolDispatcher pojo, @NonNull RequestData originReqData) {
        String requestBy = Optional.ofNullable(originReqData.headers())
                                   .map(h -> h.getString(Headers.X_REQUEST_BY))
                                   .orElse(null);
        return Strings.isNotBlank(pojo.getAddress()) &&
               !RemoteServiceInvoker.requestBy(pojo.getProtocol().type()).equals(requestBy);
    }

    private Single<VertxPojo> dispatch(@NonNull String address, @NonNull EntityRuntimeContext<VertxPojo> ctx) {
        final RequestData reqData = RequestData.builder()
                                               .body(ctx.getData().toJson())
                                               .headers(ctx.getOriginReqData().headers())
                                               .build();
        final ProtocolDispatcherRpcClient dispatcher = new ProtocolDispatcherRpcClient(transporter());
        return dispatcher.invoke(address, ctx.getOriginReqAction(), reqData)
                         .flatMap(msg -> msg.isError()
                                         ? Single.error(ErrorMessageConverter.from(msg.getError()))
                                         : Single.just(Optional.ofNullable(msg.getData()).orElse(new JsonObject())))
                         .map(ctx.getMetadata()::parseFromRequest);
    }

    @SuppressWarnings("unchecked")
    private Maybe<ProtocolDispatcher> fromCache(@NonNull EntityRuntimeContext<VertxPojo> taskData) {
        final ProtocolDispatcherCache cache = definitionContext().getSharedDataValue(
            DataCacheInitializer.PROTOCOL_DISPATCHER_CACHE);
        final Protocol protocol = taskData.getMetadata() instanceof HasProtocol
                                  ? ((HasProtocol) taskData.getMetadata()).getProtocol(taskData.getData())
                                  : Protocol.UNKNOWN;
        final ProtocolDispatcher pojo = new ProtocolDispatcher().setProtocol(protocol)
                                                                .setEntity(taskData.getMetadata().singularKeyName())
                                                                .setAction(taskData.getOriginReqAction());
        return cache.get(ProtocolDispatcherCache.serializeKey(pojo));
    }

}
