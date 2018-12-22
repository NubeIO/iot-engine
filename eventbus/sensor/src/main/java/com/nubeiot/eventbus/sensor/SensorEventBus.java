package com.nubeiot.eventbus.sensor;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensorEventBus {

    public static final EventModel SENSOR_DATA_TEST = EventModel.builder()
                                                                .address("nubeiot.sensor.test")
                                                                .event(EventAction.CREATE)
                                                                .build();

}
