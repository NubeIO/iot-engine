package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.edge.connector.bacnet.translator.BACnetDeviceTranslator;
import com.nubeiot.edge.module.datapoint.model.pojos.EdgeDeviceComposite;
import com.nubeiot.edge.module.datapoint.rpc.query.AbstractDataProtocolScanner;
import com.nubeiot.edge.module.datapoint.rpc.query.DataProtocolDeviceScanner;

import lombok.NonNull;

public final class BACnetDeviceScanner
    extends AbstractDataProtocolScanner<EdgeDeviceComposite, RemoteDeviceMixin, BACnetDeviceScanner>
    implements DataProtocolDeviceScanner<RemoteDeviceMixin, BACnetDeviceScanner>, BACnetRpcProtocol {

    BACnetDeviceScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey, new BACnetDeviceTranslator());
    }

}
