package com.nubeiot.edge.connector.bacnet.objectModels;

import org.junit.Assert;
import org.junit.Test;

public class edgeWriteRequestTest {

    @Test
    public void constructorTest() throws Exception {
        String id = "aa";
        int val = 1;
        int priority = 15;

        EdgeWriteRequest req = new EdgeWriteRequest(id, val, priority);
        Assert.assertEquals(id, req.getId());
        Assert.assertEquals(val, req.getValue());
        Assert.assertEquals(priority, req.getPriority());
    }

}
