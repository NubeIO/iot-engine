package com.nubeiot.edge.module.datapoint.model.ditto;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.sql.CompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointMetadata;
import com.nubeiot.edge.module.datapoint.cache.DataCacheInitializer;
import com.nubeiot.iotdata.edge.model.Tables;

@RunWith(VertxUnitRunner.class)
public class IDittoModelTest {

    private Vertx vertx;

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        SharedDataDelegate.addLocalDataValue(vertx, this.getClass().getName(), DataCacheInitializer.SYNC_CONFIG_CACHE,
                                             new ClassGraphCache<EntityMetadata, IDittoModel>().register(
                                                 IDittoModel::find));
    }

    @Test
    public void test_init() {
        DataPointIndex.INDEX.stream()
                            .filter(metadata -> metadata instanceof PointCompositeMetadata ||
                                                !(metadata instanceof CompositeMetadata ||
                                                  metadata instanceof PointMetadata))
                            .filter(metadata -> !(Tables.DEVICE_EQUIP.equals(metadata.table()) ||
                                                  Tables.THING.equals(metadata.table()) ||
                                                  Tables.PROTOCOL_DISPATCHER.equals(metadata.table())))
                            .map(IDittoModel::find)
                            .forEach(Assert::assertNotNull);
    }

    @Test
    public void test_get_directly() {
        Assert.assertNotNull(IDittoModel.find(PointCompositeMetadata.INSTANCE));
    }

    @Test
    public void test_get_from_cache() {
        final ClassGraphCache<EntityMetadata, IDittoModel> cache = SharedDataDelegate.getLocalDataValue(vertx,
                                                                                                        getClass().getName(),
                                                                                                        DataCacheInitializer.SYNC_CONFIG_CACHE);
        Assert.assertNotNull(cache.get(PointCompositeMetadata.INSTANCE));
        Assert.assertNotNull(cache.get(PointCompositeMetadata.INSTANCE));
    }

    private String blockingLoad(String id) {

        // Simulate a blocking action
        try {
            Thread.sleep(100);
            return "someData for " + id + " on thread " + Thread.currentThread().getName();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
