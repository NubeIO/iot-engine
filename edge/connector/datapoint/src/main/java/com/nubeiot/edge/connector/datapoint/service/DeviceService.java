package com.nubeiot.edge.connector.datapoint.service;

import com.nubeiot.iotdata.model.tables.interfaces.IDevice;

public class DeviceService extends AbstractModelService<IDevice> {

    @Override
    public String endpoint() {
        return "/device";
    }

}
