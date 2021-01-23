package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.core.rpc.query.AbstractRpcScanner;
import com.nubeiot.core.rpc.query.PointRpcScanner;
import com.nubeiot.edge.connector.bacnet.converter.BACnetPointConverter;
import com.nubeiot.edge.connector.bacnet.entity.BACnetPointEntity;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;

import lombok.NonNull;

public final class BACnetPointScanner extends AbstractRpcScanner<BACnetPointEntity, PropertyValuesMixin>
    implements PointRpcScanner<BACnetPointEntity, PropertyValuesMixin>, BACnetRpcProtocol<BACnetPointEntity> {

    protected BACnetPointScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy, new BACnetPointConverter());
    }

    @Override
    public @NonNull Class<BACnetPointEntity> context() {
        return BACnetPointEntity.class;
    }

}
