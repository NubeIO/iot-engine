package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.core.rpc.scanner.AbstractRpcScanner;
import com.nubeiot.core.rpc.scanner.DeviceRpcScanner;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDeviceConverter;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;

import lombok.NonNull;

public final class BACnetDeviceRpcScanner extends AbstractRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin>
    implements DeviceRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin>, BACnetRpcProtocol<BACnetDeviceEntity> {

    BACnetDeviceRpcScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy, new BACnetDeviceConverter());
    }

    @Override
    public @NonNull Class<BACnetDeviceEntity> context() {
        return BACnetDeviceEntity.class;
    }

}
