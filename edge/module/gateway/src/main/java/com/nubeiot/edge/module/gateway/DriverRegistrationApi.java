package com.nubeiot.edge.module.gateway;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.http.rest.AbstractRestEventApi;

public class DriverRegistrationApi extends AbstractRestEventApi {

    static EventModel model = EventModel.builder()
                                        .address("edge.module.gateway.register")
                                        .addEvents(EventAction.CREATE, EventAction.REMOVE)
                                        .build();

    @Override
    protected void initRoute() {
        this.addRouter(model, "/drivers/registration", "/drivers/registration/:event_id");
    }

}
