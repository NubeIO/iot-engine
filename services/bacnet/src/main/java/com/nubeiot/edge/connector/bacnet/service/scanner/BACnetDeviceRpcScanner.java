package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import com.nubeiot.core.rpc.query.AbstractRpcScanner;
import com.nubeiot.core.rpc.query.DeviceRpcScanner;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDeviceConverter;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;

import lombok.NonNull;

public final class BACnetDeviceRpcScanner
    extends AbstractRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin, BACnetDeviceRpcScanner>
    implements DeviceRpcScanner<BACnetDeviceEntity, RemoteDeviceMixin, BACnetDeviceRpcScanner>,
               BACnetRpcProtocol<BACnetDeviceEntity> {

    BACnetDeviceRpcScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey, new BACnetDeviceConverter());
    }

    @Override
    public @NonNull Class<BACnetDeviceEntity> context() {
        return BACnetDeviceEntity.class;
    }

}
