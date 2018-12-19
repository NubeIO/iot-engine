package com.nubeiot.eventbus.sensor;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensorEventBus {

    public static final EventModel SENSOR_DATA_TEST = new EventModel("nubeiot.sensor.test").add(EventType.CREATE);

}
