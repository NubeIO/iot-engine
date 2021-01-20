package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.unit.DataType;

/**
 * Represents a converter between normal data type and the equivalent data type in another {@code protocol}
 *
 * @param <T> Type of Data type
 * @param <U> Type of specific protocol data type
 * @see DataType
 * @since 1.0.0
 */
public interface DataTypeConverter<T extends DataType, U> extends IoTConverter<T, U> {

}
