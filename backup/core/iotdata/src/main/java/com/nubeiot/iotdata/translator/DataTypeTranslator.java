package com.nubeiot.iotdata.translator;

import com.nubeiot.iotdata.unit.DataType;

/**
 * Represents translator between IoT data type and the equivalent data type in another {@code protocol}
 *
 * @param <T> Nube IoT data type object type
 * @param <U> Protocol data type object type
 * @see DataType
 * @since 1.0.0
 */
public interface DataTypeTranslator<T extends DataType, U> extends IoTTranslator<T, U> {

}
