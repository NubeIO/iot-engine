package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.exceptions.CarlException;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedRequestService;

import lombok.NonNull;

/**
 * @param <D> Type of data to send BACnet remote device
 * @param <S> Type of BACnet Request
 * @see ConfirmedRequestService
 */
public interface ConfirmedRequestFactory<S extends ConfirmedRequestService, D> {

    /**
     * Strict mode to determined raise exception if {@code AckConfirmedRequest} is error
     */
    String STRICT = "strict";

    @NonNull D convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData);

    @NonNull S factory(@NonNull DiscoveryArguments args, @NonNull D data);

    default void then(@NonNull BACnetDevice device, @NonNull EventMessage result, @NonNull D data,
                      @NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        if (result.isError() && requestData.filter().getBoolean(STRICT, true)) {
            throw new CarlException(result.getError().getCode(), result.getError().getMessage());
        }
    }

}
