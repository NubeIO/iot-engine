package com.nubeiot.core.rpc.discovery;

import io.github.zero88.qwe.event.EventAction;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.rpc.RpcClient;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for a {@code Protocol Discovery service} that is able to discover a particular data object in {@code protocol}
 * by {@code RPC client}
 *
 * @param <P> Type of IoT entity
 * @see RpcClient
 */
public interface RpcDiscovery<P extends IoTEntity> extends RpcClient<P> {

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
