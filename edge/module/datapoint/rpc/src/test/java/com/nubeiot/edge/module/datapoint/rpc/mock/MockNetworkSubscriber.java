package com.nubeiot.edge.module.datapoint.rpc.mock;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

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
    public @NonNull Protocol protocol() {
        return Protocol.BACNET;
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
    protected Single<Network> doCreate(@NonNull Network pojo) {
        return Single.just(pojo.setMetadata(metadata));
    }

    @Override
    protected Single<Network> doUpdate(@NonNull Network pojo) {
        return null;
    }

    @Override
    protected Single<Network> doPatch(@NonNull Network pojo) {
        return null;
    }

    @Override
    protected Single<Network> doDelete(@NonNull Network pojo) {
        return null;
    }

}
