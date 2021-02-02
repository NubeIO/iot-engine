package com.nubeiot.core.rpc.coordinator;

import io.github.zero88.qwe.event.Waybill;

public interface EventbusChannel extends Channel {

    Waybill process();

    Waybill publish();

}
