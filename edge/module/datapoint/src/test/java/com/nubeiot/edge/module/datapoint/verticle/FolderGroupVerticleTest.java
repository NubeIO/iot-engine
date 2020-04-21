package com.nubeiot.edge.module.datapoint.verticle;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;

public abstract class FolderGroupVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected final JsonObject builtinData() {
        return MockData.data_Folder_Group();
    }

}
