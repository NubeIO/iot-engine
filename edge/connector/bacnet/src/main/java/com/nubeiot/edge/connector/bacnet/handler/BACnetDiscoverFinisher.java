package com.nubeiot.edge.connector.bacnet.handler;

import java.util.function.Function;

import com.nubeiot.core.component.SharedDataDelegate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class BACnetDiscoverFinisher extends DiscoverCompletionHandler
    implements SharedDataDelegate<BACnetDiscoverFinisher> {

    @NonNull
    private final Function<String, Object> sharedFunc;

    @Override
    @SuppressWarnings("unchecked")
    public <D> D getSharedDataValue(String dataKey) {
        return (D) sharedFunc.apply(dataKey);
    }

    @Override
    public BACnetDiscoverFinisher registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

}
