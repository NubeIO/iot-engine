package com.nubeiot.edge.connector.bacnet;

import com.nubeiot.edge.connector.bacnet.service.coordinator.CovCoordinatorPersistenceConfig;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class BACnetServiceConfig extends BACnetConfig {

    @NonNull
    @Default
    private final String gatewayAddress = "gateway.index";
    @NonNull
    @Default
    private final String schedulerServiceName = "bacnet-scheduler";

    @Default
    private final CovCoordinatorPersistenceConfig covCoordinatorPersistence = CovCoordinatorPersistenceConfig.def();

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
