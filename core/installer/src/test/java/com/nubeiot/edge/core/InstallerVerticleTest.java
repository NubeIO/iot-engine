package com.nubeiot.edge.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class InstallerVerticleTest {

    @Test
    public void testJson() {
        JsonObject def = new JsonObject().put("key1", "1").put("key2", 3).put("key3", 5);
        JsonObject input = new JsonObject().put("key1", "3").put("key2", 4).put("key4", "hello");
        final JsonObject result = input.mergeIn(def);
        System.out.println(def);
        System.out.println(result);
        assertEquals(input, result);
        assertNotEquals(def, result);
    }

    @Test
    public void testJsonMergeToDefault() {
        JsonObject def = new JsonObject().put("key1", "1").put("key2", 3).put("key3", 5);
        JsonObject dest = new JsonObject().put("key1", "3").put("key2", 4).put("key4", "hello");
        final JsonObject result = def.mergeIn(dest);
        System.out.println(dest);
        System.out.println(result);
        assertEquals(def, result);
        assertNotEquals(dest, result);
    }

}
