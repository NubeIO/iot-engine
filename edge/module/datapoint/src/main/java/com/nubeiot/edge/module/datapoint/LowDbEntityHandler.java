package com.nubeiot.edge.module.datapoint;

import com.nubeiot.core.event.EventModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class LowDbEntityHandler {

    private final DataPointConfig pointConfig;
    @Setter
    private EventModel schedulerRegisterModel;

}
