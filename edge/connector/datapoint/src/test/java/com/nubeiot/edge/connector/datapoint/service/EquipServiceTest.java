package com.nubeiot.edge.connector.datapoint.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.datapoint.MockData;

public class EquipServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Equip_Thing();
    }

}
