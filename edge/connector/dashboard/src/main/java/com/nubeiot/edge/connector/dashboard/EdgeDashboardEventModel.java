package com.nubeiot.edge.connector.dashboard;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

public class EdgeDashboardEventModel {

    static final EventModel EDGE_DASHBOARD_CONNECTION = EventModel.builder()
                                                                  .address(
                                                                      "nubeiot.edge.connector.dashboard.connection")
                                                                  .pattern(EventPattern.REQUEST_RESPONSE)
                                                                  .local(true)
                                                                  .addEvents(EventAction.GET_LIST, EventAction.PATCH)
                                                                  .build();

}
