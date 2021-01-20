package com.nubeiot.core.rpc.query;

import io.github.zero88.msa.bp.dto.JsonData;
import io.vertx.core.Vertx;

import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractRpcScanner<P extends JsonData, X, T extends AbstractRpcScanner>
    extends BaseRpcProtocol<P, T> implements RpcScanner<P, X, T> {

    private final IoTEntityTranslator<P, X> translator;

    protected AbstractRpcScanner(@NonNull Vertx vertx, @NonNull String sharedKey,
                                 @NonNull IoTEntityTranslator<P, X> translator) {
        super(vertx, sharedKey);
        this.translator = translator;
    }

}
