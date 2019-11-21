package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.core.enums.State;
import com.serotonin.bacnet4j.type.enumerated.DeviceStatus;

public class BACnetStateTranslator implements BACnetTranslator<State, DeviceStatus> {

    @Override
    public DeviceStatus from(State concept) {
        return null;
    }

    @Override
    public State to(DeviceStatus object) {
        if (Objects.isNull(object)) {
            return State.UNAVAILABLE;
        }
        if (DeviceStatus.operational.equals(object) || DeviceStatus.operationalReadOnly.equals(object)) {
            return State.ENABLED;
        }
        if (DeviceStatus.nonOperational.equals(object)) {
            return State.DISABLED;
        }
        if (DeviceStatus.backupInProgress.equals(object) || DeviceStatus.downloadInProgress.equals(object)) {
            return State.PENDING;
        }
        return State.NONE;
    }

    @Override
    public Class<State> fromType() {
        return State.class;
    }

    @Override
    public Class<DeviceStatus> toType() {
        return DeviceStatus.class;
    }

}
