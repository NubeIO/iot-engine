package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.edge.connector.bacnet.translator.BACnetPointTranslator;
import com.nubeiot.edge.module.datapoint.model.pojos.PointThingComposite;
import com.nubeiot.edge.module.datapoint.rpc.query.AbstractDataProtocolScanner;
import com.nubeiot.edge.module.datapoint.rpc.query.DataProtocolPointScanner;

import lombok.NonNull;

public final class BACnetPointScanner
    extends AbstractDataProtocolScanner<PointThingComposite, PropertyValuesMixin, BACnetPointScanner>
    implements DataProtocolPointScanner<PropertyValuesMixin, BACnetPointScanner>, BACnetRpcProtocol {

    protected BACnetPointScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey, new BACnetPointTranslator());
    }

}
