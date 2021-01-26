package com.nubeiot.core.rpc.discovery;

import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.protocol.Protocol;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.rpc.RpcProtocolClient;
import com.nubeiot.core.rpc.watcher.RpcWatcher;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for a {@code discovery service} is based on {@code RpcClient} that is able to scan/get/discover a
 * particular {@code protocol} data object.
 * <p>
 * It also helps registering {@link RpcWatcher} to one/many particular {@code protocol} data object if using {@link
 * #watchOne(JsonObject)} or {@link #watchMany(JsonObject)}
 *
 * @param <P> Type of IoT entity
 * @see RpcProtocolClient
 * @see Protocol
 * @see RpcWatcher
 */
public interface RpcDiscovery<P extends IoTEntity> extends RpcProtocolClient<P> {

    /**
     * Register a {@link RpcWatcher} to one particular {@code protocol} data object
     *
     * @param data given data
     * @return json result in {@link Single}
     */
    @NonNull
    default Single<JsonObject> watchOne(@NonNull JsonObject data) {
        return this.execute(watchOneAction(), data);
    }

    /**
     * Register many {@link RpcWatcher} to many particular {@code protocol} data object
     *
     * @param data given data
     * @return json result in {@link Single}
     */
    @NonNull
    default Single<JsonObject> watchMany(@NonNull JsonObject data) {
        return Single.error(new UnsupportedOperationException("Not yet supported batch service"))
                     .flatMap(unused -> this.execute(watchManyAction(), data));
    }

    @NonNull
    default EventAction watchOneAction() {
        return EventAction.CREATE;
    }

    @NonNull
    default EventAction watchManyAction() {
        return EventAction.BATCH_CREATE;
    }

}
