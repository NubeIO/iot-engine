package com.nubeiot.edge.connector.device;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DeviceEventModels {

    static final EventModel DEVICE_STATUS = EventModel.builder().address("nubeiot.edge.connector.device.status")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .local(true)
                                                      .addEvents(EventAction.GET_LIST)
                                                      .build();

    static final EventModel DEVICE_NETWORK = EventModel.builder()
                                                       .address("nubeiot.edge.connector.device.network")
                                                       .pattern(EventPattern.REQUEST_RESPONSE)
                                                       .local(true)
                                                       .addEvents(EventAction.GET_LIST)
                                                       .build();

    static final EventModel DEVICE_IP = EventModel.builder()
                                                  .address("nubeiot.edge.connector.device.ip")
                                                  .pattern(EventPattern.REQUEST_RESPONSE)
                                                  .local(true)
                                                  .addEvents(EventAction.CREATE)
                                                  .build();

    static final EventModel DEVICE_DHCP = EventModel.builder()
                                                    .address("nubeiot.edge.connector.device.dhcp")
                                                    .pattern(EventPattern.REQUEST_RESPONSE)
                                                    .local(true)
                                                    .addEvents(EventAction.CREATE)
                                                    .build();

}
