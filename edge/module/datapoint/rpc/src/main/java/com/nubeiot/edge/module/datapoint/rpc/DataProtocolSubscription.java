package com.nubeiot.edge.module.datapoint.rpc;

import java.util.Collection;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.ProtocolDispatcherMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.ProtocolDispatcher;

import lombok.NonNull;

/**
 * Represents a register service that registers {@code Subscriber} into {@code Data Point repository} in startup phase
 * of the particular {@code protocol} application
 *
 * @see DataProtocolSubscriber
 */
public interface DataProtocolSubscription<T extends DataProtocolSubscription> extends DataProtocolRpcClient<T> {

    @Override
    default @NonNull ProtocolDispatcherMetadata representation() {
        return ProtocolDispatcherMetadata.INSTANCE;
    }

    default Single<JsonObject> register(@NonNull DataProtocolSubscriber<VertxPojo> subscriber) {
        final @NonNull Collection<EventAction> actions = subscriber.getAvailableEvents();
        return Observable.fromIterable(actions.stream()
                                              .map(action -> new ProtocolDispatcher().setState(State.ENABLED)
                                                                                     .setAddress(subscriber.address())
                                                                                     .setProtocol(subscriber.protocol())
                                                                                     .setAction(action)
                                                                                     .setGlobal(subscriber.isGlobal())
                                                                                     .setEntity(subscriber.metadata()
                                                                                                          .singularKeyName()))
                                              .collect(Collectors.toList()))
                         .flatMapSingle(pojo -> execute(EventAction.CREATE_OR_UPDATE, JsonPojo.from(pojo).toJson()))
                         .collect(JsonArray::new, JsonArray::add)
                         .map(r -> new JsonObject().put(representation().pluralKeyName(), r));
    }

}
