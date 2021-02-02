package io.github.zero88.qwe.iot.connector.coordinator;

import io.github.zero88.qwe.event.Waybill;

public interface EventbusChannel extends Channel {

    Waybill process();

    Waybill publish();

}
