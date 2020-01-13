package com.nubeiot.edge.connector.bacnet.service.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.connector.bacnet.translator.BACnetNetworkTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

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
        return DataPointIndex.lookupApiName(NetworkMetadata.INSTANCE);
    }

    @Override
    public Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.createDefault("/network", "network_id"));
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.CREATE, EventAction.GET_LIST);
    }

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData reqData) {
        if (errorInCreate) {
            return Single.error(new NubeException("Failed"));
        }
        final JsonObject resource = JsonPojo.from(new Network().fromJson(reqData.body()).setId(UUID.randomUUID()))
                                            .toJson();
        return Single.just(
            new JsonObject().put("action", EventAction.CREATE).put("status", Status.SUCCESS).put("resource", resource));
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData reqData) {
        return Single.just(new JsonObject().put("networks", defaultNetworks()));
    }

    private JsonArray defaultNetworks() {
        if (!hasNetworks) {
            return new JsonArray();
        }
        final UdpProtocol protocol = UdpProtocol.builder().port(47808).canReusePort(true).ip(network).build();
        final Network network = new BACnetNetworkTranslator().serialize(protocol).setId(id);
        return new JsonArray().add(JsonPojo.from(network).toJson());
    }

}
