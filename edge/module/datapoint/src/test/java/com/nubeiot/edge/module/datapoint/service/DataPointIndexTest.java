package com.nubeiot.edge.module.datapoint.service;

import org.junit.Assert;
import org.junit.Test;

public class DataPointIndexTest {

    @Test
    public void test_how_many_model() {
        Assert.assertEquals(16, DataPointIndex.INDEX.size());
    }

}
