package com.nubeiot.edge.connector.bacnet;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public final class BACnetServiceConfig extends BACnetConfig {

    @NonNull
    @Default
    private final String gatewayAddress = "nubeio.bacnet.gateway.index";

    @Override
    public int getVendorId() {
        return 1173;
    }

    @Override
    public String getVendorName() {
        return "Nube iO Operations Pty Ltd";
    }

    @Override
    public String getModelName() {
        return super.getModelName().contains("QWE") ? "Rubix" : super.getModelName();
    }

}
