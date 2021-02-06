package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.connector.scanner.AbstractRpcScanner;
import io.github.zero88.qwe.iot.connector.scanner.PointRpcScanner;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.entity.BACnetPointEntity;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;

import lombok.NonNull;

public final class BACnetPointScanner extends AbstractRpcScanner<BACnetPointEntity, PropertyValuesMixin>
    implements PointRpcScanner<BACnetPointEntity, PropertyValuesMixin>, BACnetProtocol {

    protected BACnetPointScanner(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy, null);
    }

    @Override
    public @NonNull Class<BACnetPointEntity> context() {
        return BACnetPointEntity.class;
    }

    @Override
    public @NonNull String gatewayAddress() {
        return sharedData().getData(BACnetCacheInitializer.GATEWAY_ADDRESS);
    }

}
