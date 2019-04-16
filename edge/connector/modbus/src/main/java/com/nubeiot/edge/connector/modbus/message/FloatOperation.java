package com.nubeiot.edge.connector.modbus.message;

public interface FloatOperation extends ValueOperation {
    boolean swapRegisters();

    int byteCount();
}
