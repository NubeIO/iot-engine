package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import com.nubeiot.core.rpc.query.DeviceRpcScanner;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.edge.connector.bacnet.translator.BACnetDeviceTranslator;

import lombok.NonNull;

public final class BACnetDeviceRpcScanner
    extends AbstractDataProtocolScanner<EdgeDeviceComposite, RemoteDeviceMixin, BACnetDeviceRpcScanner>
    implements DeviceRpcScanner<RemoteDeviceMixin, BACnetDeviceRpcScanner>, BACnetRpcProtocol {

    BACnetDeviceRpcScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey, new BACnetDeviceTranslator());
    }

}
