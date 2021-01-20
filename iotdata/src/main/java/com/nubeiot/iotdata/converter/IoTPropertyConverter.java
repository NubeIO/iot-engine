package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.IoTProperty;

/**
 * Represents {@code translator} between {@code Nube IoT enum type} and the equivalent enum type in another {@code
 * protocol}*
 *
 * @param <T> Nube IoT notion type
 * @param <U> Protocol notion type
 * @see IoTProperty
 * @since 1.0.0
 */
public interface IoTPropertyConverter<T extends IoTProperty, U extends IoTProperty> extends IoTConverter<T, U> {

}
