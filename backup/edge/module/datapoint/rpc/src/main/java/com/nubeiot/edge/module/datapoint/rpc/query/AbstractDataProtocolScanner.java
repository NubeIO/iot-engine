package com.nubeiot.edge.module.datapoint.rpc.query;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.Vertx;

import com.nubeiot.edge.module.datapoint.rpc.BaseRpcProtocol;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractDataProtocolScanner<P extends VertxPojo, X, T extends AbstractDataProtocolScanner>
    extends BaseRpcProtocol<T> implements DataProtocolScanner<P, X, T> {

    private final IoTEntityTranslator<P, X> translator;

    protected AbstractDataProtocolScanner(@NonNull Vertx vertx, @NonNull String sharedKey,
                                          @NonNull IoTEntityTranslator<P, X> translator) {
        super(vertx, sharedKey);
        this.translator = translator;
    }

}
