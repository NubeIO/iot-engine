package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.connector.scanner.AbstractRpcScanner;
import io.github.zero88.qwe.iot.connector.scanner.DeviceRpcScanner;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;

import lombok.NonNull;

public final class BACnetDeviceScanner extends AbstractRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin>
    implements DeviceRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin>, BACnetProtocol {

    BACnetDeviceScanner(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData, null);
    }

    @Override
    public @NonNull Class<BACnetDeviceEntity> context() {
        return BACnetDeviceEntity.class;
    }

}
