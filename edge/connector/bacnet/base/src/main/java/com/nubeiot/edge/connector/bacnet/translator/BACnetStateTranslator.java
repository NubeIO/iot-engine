package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.core.enums.State;
import com.serotonin.bacnet4j.type.enumerated.DeviceStatus;

public final class BACnetStateTranslator implements BACnetTranslator<State, DeviceStatus> {

    @Override
    public DeviceStatus from(State concept) {
        if (Objects.isNull(concept) || concept.equals(State.UNAVAILABLE) || concept.equals(State.NONE)) {
            return null;
        }
        if (State.DISABLED.equals(concept)) {
            return DeviceStatus.nonOperational;
        }
        if (State.PENDING.equals(concept)) {
            return DeviceStatus.downloadInProgress;
        }
        return DeviceStatus.operational;
    }

    @Override
    public State to(DeviceStatus object) {
        if (Objects.isNull(object)) {
            return State.NONE;
        }
        if (DeviceStatus.nonOperational.equals(object)) {
            return State.DISABLED;
        }
        if (DeviceStatus.backupInProgress.equals(object) || DeviceStatus.downloadInProgress.equals(object)) {
            return State.PENDING;
        }
        return State.ENABLED;
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
