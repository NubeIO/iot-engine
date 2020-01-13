package com.nubeiot.edge.connector.bacnet.handler;

import java.util.function.Function;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.ErrorData;

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

    @Override
    public boolean success(@NonNull RequestData requestData) {
        return super.success(requestData);
    }

    @Override
    public boolean error(@NonNull ErrorData error) {
        return super.error(error);
    }

}
