package com.nubeiot.core.rpc.query;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.iotdata.IoTEntity;
import com.nubeiot.iotdata.converter.IoTEntityConverter;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractRpcScanner<P extends IoTEntity, X, T extends AbstractRpcScanner>
    extends BaseRpcProtocol<P> implements RpcScanner<P, X, T> {

    private final IoTEntityConverter<P, X> translator;

    protected AbstractRpcScanner(@NonNull SharedDataLocalProxy sharedDataProxy,
                                 @NonNull IoTEntityConverter<P, X> translator) {
        super(sharedDataProxy);
        this.translator = translator;
    }

}
