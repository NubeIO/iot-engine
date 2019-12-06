package com.nubeiot.edge.module.datapoint.rpc.mock;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.edge.module.datapoint.MockData.ProtocolDispatcherAddress;
import com.nubeiot.edge.module.datapoint.rpc.AbstractProtocolSubscriber;
import com.nubeiot.edge.module.datapoint.rpc.DataProtocolSubscriber;
import com.nubeiot.iotdata.dto.Protocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

public class MockNetworkSubscriber extends AbstractProtocolSubscriber<Network>
    implements DataProtocolSubscriber<Network> {

    private final JsonObject metadata;

    public MockNetworkSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey, JsonObject metadata) {
        super(vertx, sharedKey);
        this.metadata = metadata;
    }

    @Override
    public @NonNull NetworkMetadata metadata() {
        return NetworkMetadata.INSTANCE;
    }

    @Override
    public String address() {
        return ProtocolDispatcherAddress.NETWORK;
    }

    @Override
    public @NonNull Single<Network> create(@NonNull RequestData requestData) {
        final Network network = metadata().parseFromRequest(requestData.body());
        return Single.just(network.setMetadata(metadata));
    }

    @Override
    public @NonNull Single<Network> update(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public @NonNull Single<Network> patch(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public @NonNull Single<Network> delete(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public @NonNull Protocol protocol() {
        return Protocol.BACNET;
    }

}
