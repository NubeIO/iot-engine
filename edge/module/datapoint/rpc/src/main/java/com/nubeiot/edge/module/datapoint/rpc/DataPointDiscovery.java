package com.nubeiot.edge.module.datapoint.rpc;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;

import lombok.NonNull;

/**
 * Represents for a {@code Protocol Discovery service} that is able to persist data into {@code Data Point repository}
 * by {@code RPC client}
 *
 * @param <T> Type of discovery client
 * @see DataPointRpcClient
 */
public interface DataPointDiscovery<T extends DataPointDiscovery> extends DataPointRpcClient<T> {

    @NonNull
    default EventAction batchPersistAction() {
        return EventAction.BATCH_CREATE;
    }

    @NonNull
    default EventAction persistAction() {
        return EventAction.CREATE;
    }

    default Single<JsonObject> doBatch(@NonNull JsonObject data) {
        return Single.error(new UnsupportedOperationException("Not yet supported batch service"));
    }

    /**
     * Persist one object
     *
     * @param data given data
     * @return json result in {@link Single}
     */
    default Single<JsonObject> doPersist(@NonNull JsonObject data) {
        return this.execute(persistAction(), data);
    }

}
