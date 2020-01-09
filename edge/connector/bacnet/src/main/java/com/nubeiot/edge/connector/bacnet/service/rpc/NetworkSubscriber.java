package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.connector.bacnet.translator.BACnetNetworkTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.rpc.AbstractProtocolSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

//TODO implement it
public final class NetworkSubscriber extends AbstractProtocolSubscriber<Network> implements BACnetSubscriber<Network> {

    NetworkSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull NetworkMetadata metadata() {
        return NetworkMetadata.INSTANCE;
    }

    @Override
    public Single<Network> create(@NonNull RequestData requestData) {
        Network network = metadata().parseFromRequest(requestData.body());
        if (shouldSkip(network) || network.getState() != State.ENABLED) {
            return Single.just(network);
        }
        CommunicationProtocol protocol = new BACnetNetworkTranslator().deserialize(network);
        return handle(network);
    }

    @Override
    public Single<Network> update(@NonNull RequestData requestData) {
        Network network = metadata().parseFromRequest(requestData.body());
        if (shouldSkip(network)) {
            return Single.just(network);
        }
        return handle(network);
    }

    @Override
    public Single<Network> patch(@NonNull RequestData requestData) {
        Network network = metadata().parseFromRequest(requestData.body());
        return handle(network);
    }

    @Override
    public Single<Network> delete(@NonNull RequestData requestData) {
        Network network = metadata().parseFromRequest(requestData.body());
        return handle(network);
    }

    private boolean shouldSkip(@NonNull Network network) {
        return network.getProtocol() != protocol();
    }

    private Single<Network> handle(Network network) {
        return Single.just(network);
    }

}
