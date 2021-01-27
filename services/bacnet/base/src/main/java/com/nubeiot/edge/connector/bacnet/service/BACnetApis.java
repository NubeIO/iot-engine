package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.micro.http.EventHttpService;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;

/**
 * Represents {@code BACnet public API} services
 */
public interface BACnetApis extends BACnetProtocol, EventHttpService {}
