package com.nubeiot.edge.connector.bacnet.objectModels;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import io.github.zero.utils.FileUtils;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.objectModels.EdgePoint.Kind;

public class edgePointTest {

    @Test
    public void constructorTest() throws Exception {
        String id = "aa";
        String name = "bb";
        int v1 = 1;
        Integer v2 = null;
        int priority = 16;
        String kind = "yee";
        float cov = 1.1f;

        EdgePoint p = new EdgePoint(id, v1);
        Assert.assertEquals(id, p.getId());
        Assert.assertEquals(id, p.getName());
        Assert.assertEquals(v1, p.getValue());
        Assert.assertNull(p.getPriority());
        Assert.assertNull(p.getPriorityArray());

        p.setValue(v2);
        Assert.assertNull(p.getValue());

        p = new EdgePoint(id, name, v1, priority, kind, cov);
        Assert.assertEquals(id, p.getId());
        Assert.assertEquals(name, p.getName());
        Assert.assertEquals(v1, p.getValue());
        Assert.assertTrue(priority == p.getPriority());
        Assert.assertNull(p.getPriorityArray());
        Assert.assertEquals(p.getKind(), Kind.OTHER);

        p = new EdgePoint(id, name, v1, priority, "bool", cov);
        Assert.assertEquals(p.getKind(), Kind.BOOL);
        p = new EdgePoint(id, name, v1, priority, "number", cov);
        Assert.assertEquals(p.getKind(), Kind.NUMBER);
        //        p = new EdgePoint(id, name, v1, priority, kind, cov);
        //        Assert.assertEquals(p.getKind(), Kind.OTHER);
    }

    @Test
    public void fromJsonTest() throws Exception {
        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        JsonObject points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));

        for (String key : points.getMap().keySet()) {
            JsonObject j = points.getJsonObject(key);

            EdgePoint p = EdgePoint.fromJson(key, j);
            Assert.assertEquals(key, p.getId());
            Assert.assertEquals(j.getString("name"), p.getName());
            Assert.assertEquals(j.getValue("value"), p.getValue());
            try{
                j.getInteger("priority");
                Assert.assertEquals(j.getInteger("priority"), p.getPriority());
            }catch (ClassCastException e){}
            if (j.containsKey("historySettings") && j.getJsonObject("historySettings").containsKey("tolerance")) {
                Assert.assertEquals(j.getJsonObject("historySettings").getFloat("tolerance"), p.getCovTolerance());
            }

            if(j.containsKey("priorityArray")) {
                JsonObject arr = j.getJsonObject("priorityArray");
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
    }

}
