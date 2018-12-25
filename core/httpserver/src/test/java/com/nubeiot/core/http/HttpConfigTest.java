package com.nubeiot.core.http;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class HttpConfigTest {

    @Test
    public void test_default_options() {
        System.out.println(JsonObject.mapFrom(new HttpConfig()).encodePrettily());
    }

}