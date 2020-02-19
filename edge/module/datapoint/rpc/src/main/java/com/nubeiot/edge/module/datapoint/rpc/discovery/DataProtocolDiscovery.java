package com.nubeiot.edge.module.datapoint.rpc.discovery;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.rpc.DataProtocolRpcClient;

import lombok.NonNull;

/**
 * Represents for a {@code Protocol Discovery service} that is able to persist data into {@code Data Point repository}
 * by {@code RPC client}
 *
 * @param <T> Type of discovery client
 * @see DataProtocolRpcClient
 */
public interface DataProtocolDiscovery<T extends DataProtocolDiscovery> extends DataProtocolRpcClient<T> {

    @NonNull
    default EventAction batchPersistAction() {
        return EventAction.BATCH_CREATE;
    }

    @NonNull
    default EventAction persistAction() {
        return EventAction.CREATE;
    }

    @NonNull
    default Single<JsonObject> doBatch(@NonNull JsonObject data) {
        return Single.error(new UnsupportedOperationException("Not yet supported batch service"));
    }

    /**
     * Persist one object
     *
     * @param data given data
     * @return json result in {@link Single}
     */
    @NonNull
    default Single<JsonObject> doPersist(@NonNull JsonObject data) {
        return this.execute(persistAction(), data);
    }

}
