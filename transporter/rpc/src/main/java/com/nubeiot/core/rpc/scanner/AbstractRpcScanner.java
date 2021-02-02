package com.nubeiot.core.rpc.scanner;

import java.util.Arrays;
import java.util.Collection;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.event.EventAction;

import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.iotdata.IoTEntity;
import com.nubeiot.iotdata.converter.IoTEntityConverter;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractRpcScanner<P extends IoTEntity, X> extends BaseRpcProtocol<P>
    implements RpcScanner<P, X> {

    public static final String SCANNER_SOURCE_KEY = "SCANNER_SOURCE";
    private static final String SCANNER_DESTINATION = "SCANNER_DESTINATION";
    private final IoTEntityConverter<P, X> converter;

    protected AbstractRpcScanner(@NonNull SharedDataLocalProxy sharedDataProxy,
                                 @NonNull IoTEntityConverter<P, X> converter) {
        super(sharedDataProxy);
        this.converter = converter;
    }

    public ScannerSource source() {
        return sharedData().getData(SCANNER_SOURCE_KEY);
    }

    @Override
    public @NonNull String destination() {
        return sharedData().getData(SCANNER_DESTINATION);
    }

    @Override
    public String function() {
        return "scanner";
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE);
    }

}
