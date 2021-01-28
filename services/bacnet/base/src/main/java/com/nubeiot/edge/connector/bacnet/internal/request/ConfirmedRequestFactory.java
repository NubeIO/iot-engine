package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.msg.RequestData;

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

}
