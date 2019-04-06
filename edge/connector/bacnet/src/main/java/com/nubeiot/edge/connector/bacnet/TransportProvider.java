package com.nubeiot.edge.connector.bacnet;

import java.util.function.Supplier;

import com.serotonin.bacnet4j.transport.Transport;

public interface TransportProvider extends Supplier<Transport> {

}
