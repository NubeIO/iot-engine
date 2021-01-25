package com.nubeiot.edge.connector.bacnet.service.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.event.EventListener;
import io.github.zero88.qwe.event.Status;
import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.micro.http.EventHttpService;
import io.github.zero88.qwe.micro.http.EventMethodDefinition;
import io.github.zero88.qwe.protocol.network.Ipv4Network;
import io.github.zero88.qwe.protocol.network.UdpProtocol;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Builder
@Accessors(fluent = true)
public class MockNetworkPersistService implements EventListener, EventHttpService {

    @Getter
    private final UUID id = UUID.randomUUID();
    private final Ipv4Network network = Ipv4Network.getFirstActiveIp();
    private final boolean hasNetworks;
    @Setter
    private boolean errorInCreate;

    @Override
    public String api() {
        //        return DataPointApiService.DEFAULT.lookupApiName(NetworkMetadata.INSTANCE);
        return "";
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.createDefault("/network", "network_id"));
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.GET_LIST);
    }

    @EventContractor(action = "CREATE", returnType = Single.class)
    public Single<JsonObject> create(RequestData reqData) {
        if (errorInCreate) {
            return Single.error(new CarlException("Failed"));
        }
        //        final JsonObject resource = JsonPojo.from(new Network().fromJson(reqData.body()).setId(UUID
        //        .randomUUID()))
        //                                            .toJson();
        return Single.just(new JsonObject().put("action", EventAction.CREATE)
                                           .put("status", Status.SUCCESS)
                                           .put("resource", "resource"));
    }

    @EventContractor(action = "GET_LIST", returnType = Single.class)
    public Single<JsonObject> list(RequestData reqData) {
        return Single.just(new JsonObject().put("networks", defaultNetworks()));
    }

    private JsonArray defaultNetworks() {
        if (!hasNetworks) {
            return new JsonArray();
        }
        final UdpProtocol protocol = UdpProtocol.builder().port(47808).canReusePort(true).ip(network).build();
        //        final Network network = new BACnetNetworkConverter().serialize(protocol).setId(id);
        //        return new JsonArray().add(JsonPojo.from(network).toJson());
        return new JsonArray().add(protocol.toJson());
    }

}
