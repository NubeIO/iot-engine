package com.nubeiot.edge.module.datapoint.rpc;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.NonNull;

/**
 * Represents a register service that registers {@code Subscriber} in {@code Data Point repository} when startup the
 * particular {@code protocol application}
 *
 * @see DataPointSubscriber
 */
public interface ProtocolDispatcherRegister<T extends ProtocolDispatcherRegister> extends DataPointRpcClient<T> {

    @Override
    default @NonNull ProtocolDispatcherMetadata representation() {
        return ProtocolDispatcherMetadata.INSTANCE;
    }

    default Single<JsonObject> register(@NonNull ProtocolDispatcher pojo) {
        return execute(EventAction.CREATE, JsonPojo.from(pojo).toJson());
    }

}
