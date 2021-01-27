package com.nubeiot.edge.connector.bacnet.dto;

import java.util.concurrent.TimeUnit;

import io.github.zero88.qwe.dto.JsonData;
import io.vertx.core.shareddata.Shareable;

import com.nubeiot.edge.connector.bacnet.AbstractBACnetConfig;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public final class LocalDeviceMetadata implements JsonData, Shareable {

    public static final int VENDOR_ID = 1173;
    public static final String VENDOR_NAME = "Nube iO Operations Pty Ltd";
    /**
     * The constant METADATA_KEY in cache.
     */
    public static final String METADATA_KEY = "LOCAL_BACNET_METADATA";

    @Default
    private final int vendorId = VENDOR_ID;
    @Default
    private final String vendorName = VENDOR_NAME;
    private final int deviceNumber;
    private final String modelName;
    private final String objectName;
    private final boolean slave;
    private final long maxTimeoutInMS;
    private final String discoverCompletionAddress;

    public static LocalDeviceMetadata from(@NonNull AbstractBACnetConfig config) {
        final long maxTimeout = TimeUnit.MILLISECONDS.convert(config.getMaxDiscoverTimeout(),
                                                              config.getMaxDiscoverTimeoutUnit());
        return LocalDeviceMetadata.builder()
                                  .deviceNumber(config.getDeviceId())
                                  .modelName(config.getModelName())
                                  .objectName(config.getDeviceName())
                                  .slave(config.isAllowSlave())
                                  .maxTimeoutInMS(maxTimeout)
                                  .discoverCompletionAddress(config.getCompleteDiscoverAddress())
                                  .build();
    }

}
