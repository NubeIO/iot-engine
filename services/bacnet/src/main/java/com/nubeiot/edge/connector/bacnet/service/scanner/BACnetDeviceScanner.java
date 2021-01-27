package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.core.rpc.scanner.AbstractRpcScanner;
import com.nubeiot.core.rpc.scanner.DeviceRpcScanner;
import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDeviceConverter;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;

import lombok.NonNull;

public final class BACnetDeviceScanner extends AbstractRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin>
    implements DeviceRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin>, BACnetProtocol {

    BACnetDeviceScanner(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData, new BACnetDeviceConverter());
    }

    @Override
    public @NonNull Class<BACnetDeviceEntity> context() {
        return BACnetDeviceEntity.class;
    }

}
