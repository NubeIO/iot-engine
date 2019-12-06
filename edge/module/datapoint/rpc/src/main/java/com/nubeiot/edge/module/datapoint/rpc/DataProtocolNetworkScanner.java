package com.nubeiot.edge.module.datapoint.rpc;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

import lombok.NonNull;

/**
 * Represents a service that scans {@code network} in {@code Data Point repository} when startup the specified {@code
 * protocol application}
 */
public interface DataProtocolNetworkScanner<T extends DataProtocolNetworkScanner> extends DataProtocolRpcClient<T> {

    @Override
    default @NonNull NetworkMetadata representation() {
        return NetworkMetadata.INSTANCE;
    }

    /**
     * Do scan network in repository
     *
     * @return list of communication protocol
     * @see CommunicationProtocol
     */
    default @NonNull Single<List<CommunicationProtocol>> scan() {
        final Network network = new Network().setProtocol(protocol()).setState(State.ENABLED);
        final RequestData req = RequestData.builder().body(JsonPojo.from(network).toJson()).build();
        return execute(EventAction.GET_LIST, req).map(
            resp -> resp.getJsonArray(representation().pluralKeyName(), new JsonArray())).map(this::convert);
    }

    default List<CommunicationProtocol> convert(JsonArray array) {
        return array.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(json -> (Network) representation().parseFromEntity((JsonObject) json))
                    .map(network -> translator().deserialize(network))
                    .collect(Collectors.toList());
    }

    /**
     * Declares {@code Protocol Translator} to deserialize {@code Network} entity to {@code communication protocol}
     * corresponding to {@link #protocol()}
     *
     * @return protocol translator
     * @see IoTEntityTranslator
     */
    @NonNull IoTEntityTranslator<Network, CommunicationProtocol> translator();

}
