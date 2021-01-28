package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.msg.RequestData;

import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.NonNull;

/**
 * @see <a href="https://store.chipkin.com/articles/bacnet-what-is-the-bacnet-change-of-value-cov">COV</a>
 */
public class SubscribeCOVRequestFactory implements ConfirmedRequestFactory<SubscribeCOVRequest, Boolean> {

    @Override
    public Boolean convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        return requestData.body().getBoolean("unsubscribe", true);
    }

    @Override
    public @NonNull SubscribeCOVRequest factory(@NonNull DiscoveryArguments args,
                                                @NonNull java.lang.Boolean unsubscribe) {
        if (unsubscribe) {
            return new SubscribeCOVRequest(new UnsignedInteger(1), args.params().objectCode(), null, null);
        }
        //Not sure first arg
        //lifetime = 0 means no expiry, in seconds
        return new SubscribeCOVRequest(new UnsignedInteger(1), args.params().objectCode(),
                                       com.serotonin.bacnet4j.type.primitive.Boolean.TRUE, new UnsignedInteger(0));
    }

}
