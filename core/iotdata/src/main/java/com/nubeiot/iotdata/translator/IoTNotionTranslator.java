package com.nubeiot.iotdata.translator;

import com.nubeiot.iotdata.dto.IoTNotion;

/**
 * Represents {@code translator} between {@code Nube IoT enum type} and the equivalent enum type in another {@code
 * protocol}*
 *
 * @param <T> Nube IoT notion type
 * @param <U> Protocol notion type
 * @see IoTNotion
 * @since 1.0.0
 */
public interface IoTNotionTranslator<T extends IoTNotion, U> extends IoTTranslator<T, U> {

}
