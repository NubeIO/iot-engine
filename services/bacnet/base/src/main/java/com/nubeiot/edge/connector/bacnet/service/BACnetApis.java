package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.iot.connector.ConnectorServiceApis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryParams;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetJsonMixin;

import lombok.NonNull;

/**
 * Represents {@code BACnet public API} services
 */
public interface BACnetApis extends BACnetProtocol, ConnectorServiceApis {

    @Override
    default ObjectMapper mapper() {
        return BACnetJsonMixin.MAPPER;
    }

    @Override
    default @NonNull String servicePath() {
        return DiscoveryParams.genServicePath(level());
    }

    @Override
    default String paramPath() {
        return DiscoveryParams.genParamPath(level());
    }

    @NonNull DiscoveryLevel level();

}
