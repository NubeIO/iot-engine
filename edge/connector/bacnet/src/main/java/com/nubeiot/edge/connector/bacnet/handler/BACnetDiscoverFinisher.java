package com.nubeiot.edge.connector.bacnet.handler;

import java.util.function.Function;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;

import lombok.NonNull;

public final class BACnetDiscoverFinisher extends DiscoverCompletionHandler
    implements SharedDataDelegate<BACnetDiscoverFinisher> {

    public boolean receive(RequestData requestData) {
        logger.info(requestData.toJson());
        return true;
    }

    @Override
    public <D> D getSharedDataValue(String dataKey) {
        return null;
    }

    @Override
    public BACnetDiscoverFinisher registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return null;
    }

}
