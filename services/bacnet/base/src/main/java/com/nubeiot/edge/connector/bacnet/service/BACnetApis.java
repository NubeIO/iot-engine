package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.micro.http.EventHttpService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetJsonMixin;

/**
 * Represents {@code BACnet public API} services
 */
public interface BACnetApis extends BACnetProtocol, EventHttpService {

    @Override
    default ObjectMapper mapper() {
        return BACnetJsonMixin.MAPPER;
    }

}
