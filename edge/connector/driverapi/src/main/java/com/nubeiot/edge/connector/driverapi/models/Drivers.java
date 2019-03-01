package com.nubeiot.edge.connector.driverapi.models;

public enum Drivers {

    BACNET("bacnet"), MODBUS("modbus"), HAYSTACK("haystack");

    public final String driver;

    private Drivers(String driver) {
        this.driver = driver;
    }
}
