package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventMessage;

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

    @NonNull D convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData);

    @NonNull S factory(@NonNull DiscoveryArguments args, @NonNull D data);

    default void then(@NonNull BACnetDevice device, @NonNull EventMessage result, @NonNull D data,
                      @NonNull DiscoveryArguments args, @NonNull RequestData requestData) { }

}
