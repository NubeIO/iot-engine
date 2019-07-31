package com.nubeiot.edge.connector.datapoint;

import io.vertx.core.shareddata.Shareable;

import com.nubeiot.auth.ExternalServer;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;

import lombok.Getter;

@Getter
public final class DataPointConfig implements IConfig {

    private LowDbMigration lowDbMigration;
    private ExternalServer syncServer;

    @Override
    public String name() {
        return "__datapoint__";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

    @Getter
    public static final class LowDbMigration implements Shareable {

        private boolean enabled = false;
        private String path = "";

    }

}
