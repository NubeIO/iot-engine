package com.nubeiot.edge.connector.modbus.message;

public interface IntegerOperation extends ValueOperation {
    boolean unsigned();

    boolean swapBytes();

    int byteCount();

    boolean swapRegisters();
}
