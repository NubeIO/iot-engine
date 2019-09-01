package com.nubeiot.edge.module.datapoint.model.ditto;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.service.DataPointIndex.PointMetadata;
import com.nubeiot.iotdata.edge.model.Tables;

public class IDittoModelTest {

    @Test
    public void test_init() {
        DataPointIndex.INDEX.stream()
                            .filter(metadata -> metadata instanceof PointCompositeMetadata ||
                                                !(metadata instanceof CompositeMetadata ||
                                                  metadata instanceof PointMetadata))
                            .filter(metadata -> !(Tables.DEVICE_EQUIP.equals(metadata.table()) ||
                                                  Tables.THING.equals(metadata.table())))
                            .map(metadata -> IDittoModel.create(metadata, new JsonObject()))
                            .forEach(Assert::assertNotNull);
    }

}
