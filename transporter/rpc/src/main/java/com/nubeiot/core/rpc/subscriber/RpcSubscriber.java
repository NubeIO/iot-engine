package com.nubeiot.core.rpc.subscriber;

import java.util.Collection;

import io.github.zero88.msa.bp.dto.JsonData;
import io.github.zero88.msa.bp.dto.msg.RequestData;
import io.github.zero88.msa.bp.event.EventAction;
import io.github.zero88.msa.bp.event.EventListener;
import io.github.zero88.msa.bp.micro.metadata.ActionMethodMapping;
import io.reactivex.Single;

import com.nubeiot.core.rpc.RpcProtocol;

import lombok.NonNull;

/**
 * Represents {@code subscriber} that listens a {@code dispatched event} from {@code NubeIO service} then do dispatch to
 * the corresponding to {@code Protocol} service
 *
 * @param <P> Type of entity object
 * @see EventListener
 * @see JsonData
 */
public interface RpcSubscriber<P extends JsonData> extends EventListener, RpcProtocol<P> {

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
     * Defines whether listening global event in {@code declared entity} regardless if entity protocol isn't matched
     * with declared protocol
     *
     * @return {@code true} if global
     * @see #protocol()
     * @see #context()
     */
    default boolean isGlobal() {
        return false;
    }

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#CREATE
     */
    @NonNull Single<P> create(@NonNull RequestData requestData);

    /**
     * Defines listener for updating existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#UPDATE
     */
    @NonNull Single<P> update(@NonNull RequestData requestData);

    /**
     * Defines listener for patching existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#PATCH
     */
    @NonNull Single<P> patch(@NonNull RequestData requestData);

    /**
     * Defines listener for deleting existing resource by primary key
     *
     * @param requestData Request data
     * @return json object that includes status message
     * @see EventAction#REMOVE
     */
    @NonNull Single<P> delete(@NonNull RequestData requestData);

}
