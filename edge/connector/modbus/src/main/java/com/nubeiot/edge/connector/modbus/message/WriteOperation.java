package com.nubeiot.edge.connector.modbus.message;

public interface WriteOperation<T> extends ValueOperation {
    T value();
}
