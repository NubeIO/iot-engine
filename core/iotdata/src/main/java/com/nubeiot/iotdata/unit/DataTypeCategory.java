package com.nubeiot.iotdata.unit;

public interface DataTypeCategory extends DataType {

    <T extends DataType, V extends DataType> T convert(V v);

    interface Power extends DataTypeCategory {}


    interface Pressure extends DataTypeCategory {}


    interface Temperature extends DataTypeCategory {}


    interface Velocity extends DataTypeCategory {}


    interface Illuminance extends DataTypeCategory {}

}
