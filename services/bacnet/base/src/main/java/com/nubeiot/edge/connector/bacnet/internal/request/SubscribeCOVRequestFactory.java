package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;

import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.internal.request.SubscribeCOVRequestFactory.SubscribeCOVParams;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * @see <a href="https://store.chipkin.com/articles/bacnet-what-is-the-bacnet-change-of-value-cov">COV</a>
 */
public class SubscribeCOVRequestFactory implements ConfirmedRequestFactory<SubscribeCOVRequest, SubscribeCOVParams> {

    @Override
    public SubscribeCOVParams convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        return JsonData.from(requestData.body(), SubscribeCOVParams.class);
    }

    @Override
    public @NonNull SubscribeCOVRequest factory(@NonNull DiscoveryArguments args, @NonNull SubscribeCOVParams params) {
        if (!params.isSubscribe()) {
            return new SubscribeCOVRequest(new UnsignedInteger(params.getProcessId()), args.params().objectCode(), null,
                                           null);
        }
        //Not sure first arg
        //lifetime = 0 means no expiry, in seconds
        return new SubscribeCOVRequest(new UnsignedInteger(params.getProcessId()), args.params().objectCode(),
                                       Boolean.TRUE, new UnsignedInteger(params.getLifetime()));
    }

    @Data
    @Builder
    @Jacksonized
    public static class SubscribeCOVParams implements JsonData {

        private final boolean subscribe;
        private final int processId;
        private final int lifetime;

    }

}
