package com.nubeiot.edge.module.datapoint;

import org.junit.Assert;
import org.junit.Test;

public class DataPointIndexTest {

    @Test
    public void test_how_many_model() {
        Assert.assertEquals(19, DataPointIndex.INDEX.size());
    }

}
