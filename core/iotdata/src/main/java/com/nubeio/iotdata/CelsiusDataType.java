package com.nubeio.iotdata;

public final class CelsiusDataType extends NumberDataType implements Temperature {

    CelsiusDataType() {
        super("CELSIUS", "U+2103");
    }

    @Override
    public Double parse(Object data) {
        return null;
    }

    @Override
    public String display(Double value) {
        return null;
    }

}
