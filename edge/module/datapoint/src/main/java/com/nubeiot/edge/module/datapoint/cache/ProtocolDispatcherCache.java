package com.nubeiot.edge.module.datapoint.cache;

import java.util.Optional;

import io.github.zero.utils.Functions;
import io.github.zero.utils.Strings;
import io.reactivex.Maybe;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.AsyncLocalCache;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.edge.module.datapoint.service.ProtocolDispatcherService;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProtocolDispatcherCache
    extends AbstractLocalCache<String, Maybe<ProtocolDispatcher>, ProtocolDispatcherCache>
    implements AsyncLocalCache<String, ProtocolDispatcher> {

    private static final String SEP = "::@@::";

    static ProtocolDispatcherCache init(@NonNull EntityHandler handler) {
        return new ProtocolDispatcherCache().register(key -> find(key, handler.eventClient()));
    }

    public static String serializeKey(@NonNull ProtocolDispatcher dispatcher) {
        return dispatcher.getProtocol() + SEP +
               Strings.requireNotBlank(dispatcher.getEntity(), "Entity cannot be blank") + SEP +
               Optional.ofNullable(dispatcher.getAction()).orElse(EventAction.UNKNOWN);
    }

    private static ProtocolDispatcher deserializeKey(@NonNull String key) {
        final String[] split = key.split(SEP, 3);
        final Protocol protocol = Protocol.factory(split[0]);
        final boolean global = protocol == Protocol.UNKNOWN;
        return new ProtocolDispatcher().setProtocol(global ? null : protocol)
                                       .setGlobal(global)
                                       .setAction(EventAction.parse(Functions.getOrDefault("", () -> split[2])))
                                       .setEntity(Strings.requireNotBlank(Functions.getOrDefault("", () -> split[1]),
                                                                          "Entity cannot be blank"));
    }

    private static Maybe<ProtocolDispatcher> find(String protocolKey, EventbusClient client) {
        final ProtocolDispatcher pojo = deserializeKey(protocolKey);
        final RequestData reqData = RequestData.builder()
                                               .filter(JsonPojo.from(pojo.setState(State.ENABLED)).toJson())
                                               .build();
        return client.request(ProtocolDispatcherService.class.getName(),
                              EventMessage.initial(EventAction.GET_LIST, reqData))
                     .map(EventMessage::getData)
                     .map(response -> response.getJsonArray(ProtocolDispatcherMetadata.INSTANCE.pluralKeyName()))
                     .map(array -> Functions.getIfThrow(() -> array.getJsonObject(0)))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .map(ProtocolDispatcher::new);
    }

    @Override
    protected String keyLabel() {
        return "ProtocolDispatcher key";
    }

    @Override
    protected String valueLabel() {
        return ProtocolDispatcher.class.getSimpleName();
    }

}
