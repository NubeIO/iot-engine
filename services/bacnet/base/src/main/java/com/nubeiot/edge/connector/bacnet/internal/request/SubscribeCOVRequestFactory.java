package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventMessage;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.internal.listener.CovNotifier;
import com.nubeiot.edge.connector.bacnet.internal.request.SubscribeCOVRequestFactory.SubscribeCOVOptions;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * @see <a href="https://store.chipkin.com/articles/bacnet-what-is-the-bacnet-change-of-value-cov">COV</a>
 */
public class SubscribeCOVRequestFactory implements ConfirmedRequestFactory<SubscribeCOVRequest, SubscribeCOVOptions> {

    @Override
    public SubscribeCOVOptions convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        return JsonData.from(requestData.filter(), SubscribeCOVOptions.class);
    }

    @Override
    public @NonNull SubscribeCOVRequest factory(@NonNull DiscoveryArguments args,
                                                @NonNull SubscribeCOVOptions options) {
        if (!options.isSubscribe()) {
            return new SubscribeCOVRequest(new UnsignedInteger(options.getProcessId()), args.params().objectCode(),
                                           null, null);
        }
        //Not sure first arg
        //lifetime = 0 means no expiry, in seconds
        return new SubscribeCOVRequest(new UnsignedInteger(options.getProcessId()), args.params().objectCode(),
                                       Boolean.TRUE, new UnsignedInteger(options.getLifetime()));
    }

    @Override
    public void then(@NonNull BACnetDevice device, @NonNull EventMessage result, @NonNull SubscribeCOVOptions options,
                     @NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        final CovNotifier notifier = device.lookupListener(CovNotifier.class, () -> new CovNotifier(device));
        if (options.isSubscribe()) {
            notifier.addDispatcher(result, args, requestData);
        } else {
            notifier.removeDispatcher(result, args, requestData);
        }
    }

    @Data
    @Builder
    @Jacksonized
    public static class SubscribeCOVOptions implements JsonData {

        private final boolean subscribe;

        /**
         * Subscriber process id
         *
         * @apiNote Use {@link LocalDevice#getInstanceNumber()}
         */
        @Default
        private final int processId = 1;

        /**
         * Lifetime is {@code 0} mean no expire
         */
        @Default
        private final int lifetime = 0;

    }

}
