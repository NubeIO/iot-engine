package com.nubeiot.edge.connector.datapoint;

import com.nubeiot.core.event.EventModel;

interface InternalDataPointEntityHandler extends DataPointEntityHandler {

    void setSchedulerRegisterModel(EventModel eventModel);

}
