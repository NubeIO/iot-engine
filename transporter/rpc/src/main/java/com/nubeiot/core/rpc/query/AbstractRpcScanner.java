package com.nubeiot.core.rpc.query;

import io.vertx.core.Vertx;

import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.iotdata.IoTEntity;
import com.nubeiot.iotdata.converter.IoTEntityConverter;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractRpcScanner<P extends IoTEntity, X, T extends AbstractRpcScanner>
    extends BaseRpcProtocol<P, T> implements RpcScanner<P, X, T> {

    private final IoTEntityConverter<P, X> translator;

    protected AbstractRpcScanner(@NonNull Vertx vertx, @NonNull String sharedKey,
                                 @NonNull IoTEntityConverter<P, X> translator) {
        super(vertx, sharedKey);
        this.translator = translator;
    }

}
