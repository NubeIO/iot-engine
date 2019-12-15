package com.nubeiot.iotdata.translator;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

/**
 * Represents {@code translator} between {@code Nube IoT database entity} and the equivalent entity in another {@code
 * protocol}*
 *
 * @param <T> Nube IoT database entity type
 * @param <U> Protocol entity type
 * @since 1.0.0
 */
public interface IoTEntityTranslator<T extends VertxPojo, U> extends IoTTranslator<T, U> {

}
