package com.nubeiot.edge.connector.datapoint.service;

import com.nubeiot.iotdata.model.tables.interfaces.IHistorySetting;

public class HistorySettingService extends AbstractModelService<IHistorySetting> {

    @Override
    public String endpoint() {
        return "/point/<point_code>/settings/history";
    }

}
