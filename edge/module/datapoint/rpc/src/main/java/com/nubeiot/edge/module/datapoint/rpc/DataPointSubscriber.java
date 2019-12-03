package com.nubeiot.edge.module.datapoint.rpc;

import java.util.Collection;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.ActionMethodMapping;

import lombok.NonNull;

/**
 * Represents {@code subscriber} that listens a {@code dispatched event} from {@code NubeIO service} then do dispatch to
 * the corresponding to {@code Protocol} service
 *
 * @param <P> Type of entity object
 * @see EventListener
 * @see VertxPojo
 */
public interface DataPointSubscriber<P extends VertxPojo> extends EventListener, RpcProtocol {

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.DML_MAP.get().keySet();
    }

    /**
     * Defines itself address in eventbus network
     *
     * @return Eventbus address
     */
    default String address() {
        return this.getClass().getName();
    }

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#CREATE
     */
    @NonNull Single<P> create(RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     */
    @NonNull Single<P> update(RequestData requestData);

    /**
     * Defines listener for patching existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#PATCH
     */
    @NonNull Single<P> patch(RequestData requestData);

    /**
     * Defines listener for deleting existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#REMOVE
     */
    @NonNull Single<P> delete(RequestData requestData);

}
