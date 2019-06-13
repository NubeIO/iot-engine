package com.nubeiot.edge.connector.bacnet.objectModels;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.FileUtils;

public class edgePointTest {

    @Test
    public void constructorTest() throws Exception {
        String id = "aa";
        String name = "bb";
        int v1 = 1;
        Integer v2 = null;
        int priority = 16;
        float cov = 1.1f;

        EdgePoint p = new EdgePoint(id, v1);
        Assert.assertEquals(id, p.getId());
        Assert.assertEquals(id, p.getName());
        Assert.assertEquals(v1, p.getValue());
        Assert.assertNull(p.getPriority());
        Assert.assertNull(p.getPriorityArray());

        p.setValue(v2);
        Assert.assertNull(p.getValue());

        p = new EdgePoint(id, name, v1, priority, cov);
        Assert.assertEquals(id, p.getId());
        Assert.assertEquals(name, p.getName());
        Assert.assertEquals(v1, p.getValue());
        Assert.assertTrue(priority == p.getPriority());
        Assert.assertNull(p.getPriorityArray());
    }

    @Test
    public void fromJsonTest() throws Exception {
        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        JsonObject points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));

        JsonObject UO1 = points.getJsonObject("UO1");
        EdgePoint p = EdgePoint.fromJson("UO1", UO1);

        Assert.assertEquals("UO1", p.getId());
        Assert.assertEquals(UO1.getString("name"), p.getName());
        Assert.assertEquals(UO1.getValue("value"), p.getValue());
        Assert.assertEquals(UO1.getInteger("priority"), p.getPriority());
        Assert.assertTrue(0f == p.getCovTolerance());

        JsonObject arr = UO1.getJsonObject("priorityArray");
        Object[] arr2 = p.getPriorityArray();
        for (int i = 0; i < 16; i++) {
            Object o = arr.getValue(Integer.toString(i + 1));
            if (o instanceof String && ((String) o).equalsIgnoreCase("null")) {
                continue;
            }
            Assert.assertEquals(o, arr2[i]);
        }
    }

}
