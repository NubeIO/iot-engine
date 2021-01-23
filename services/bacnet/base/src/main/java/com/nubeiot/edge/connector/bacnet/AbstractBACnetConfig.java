package com.nubeiot.edge.connector.bacnet;

import java.util.concurrent.TimeUnit;

import io.github.zero88.qwe.CarlConfig.AppConfig;
import io.github.zero88.qwe.IConfig;
import io.github.zero88.utils.Strings;

import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true)
public abstract class AbstractBACnetConfig implements IConfig {

    protected static final int MIN_DEVICE_ID = 80000;
    protected static final int MAX_DEVICE_ID = 90000;

    @Setter(value = AccessLevel.PACKAGE)
    private int deviceId;
    private String modelName = "NubeIO-Edge28";
    private String deviceName;
    @Setter(value = AccessLevel.PACKAGE)
    private boolean allowSlave = true;
    @Setter(value = AccessLevel.PACKAGE)
    private long maxDiscoverTimeout = 6;
    @Setter(value = AccessLevel.PACKAGE)
    private TimeUnit maxDiscoverTimeoutUnit = TimeUnit.SECONDS;
    private String completeDiscoverAddress = AbstractBACnetConfig.class.getPackage().getName() + ".discover.complete";
    private String readinessAddress = AbstractBACnetConfig.class.getPackage().getName() + ".readiness";
    private boolean enableSubscriber = false;

    @Override
    public final String key() {
        return "__bacnet__";
    }

    @Override
    public final Class<? extends IConfig> parent() { return AppConfig.class; }

    public int getDeviceId() {
        if (deviceId < 0 || deviceId > ObjectIdentifier.UNINITIALIZED) {
            throw new IllegalArgumentException("Illegal device id: " + deviceId);
        }
        deviceId = deviceId == 0 ? genDeviceId() : deviceId;
        return deviceId;
    }

    public String getDeviceName() {
        deviceName = Strings.isBlank(deviceName) ? modelName + "-" + deviceId : deviceName;
        return deviceName;
    }

    protected abstract int maxDeviceId();

    protected abstract int minDeviceId();

    private int genDeviceId() {
        return (int) (Math.random() * (maxDeviceId() - minDeviceId() + 1)) + minDeviceId();
    }

}
