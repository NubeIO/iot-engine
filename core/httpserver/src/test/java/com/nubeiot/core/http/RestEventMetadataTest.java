package com.nubeiot.core.http;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.event.EventAction;

import io.vertx.core.http.HttpMethod;

public class RestEventMetadataTest {

    private RestEventMetadata createMetadata(EventAction event, HttpMethod method) {
        return RestEventMetadata.builder()
                                .action(event)
                                .address("address.1")
                                .method(method)
                                .path("/api/gold")
                                .paramName("gold_id")
                                .build();
    }

    @Test
    public void test_post() {
        RestEventMetadata metadata = createMetadata(EventAction.CREATE, HttpMethod.POST);
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_list() {
        RestEventMetadata metadata = createMetadata(EventAction.GET_LIST, HttpMethod.GET);
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_one() {
        RestEventMetadata metadata = createMetadata(EventAction.GET_ONE, HttpMethod.GET);
        Assert.assertEquals("/api/gold/:gold_id", metadata.getPath());
    }

    @Test
    public void test_other_method() {
        RestEventMetadata metadata = createMetadata(EventAction.INIT, HttpMethod.OPTIONS);
        Assert.assertEquals("/api/gold", metadata.getPath());
        Assert.assertEquals(HttpMethod.OPTIONS, metadata.getMethod());
    }

    @Test
    public void test_custom_gen_path() {
        RestEventMetadata metadata = RestEventMetadata.builder()
                                                      .action(EventAction.GET_ONE)
                                                      .address("address.1")
                                                      .method(HttpMethod.POST)
                                                      .path("/api/translate")
                                                      .generatePath(RestEventMetadata::rawPath)
                                                      .build();
        Assert.assertEquals("/api/translate", metadata.getPath());
        Assert.assertEquals(HttpMethod.POST, metadata.getMethod());
        Assert.assertEquals(EventAction.GET_ONE, metadata.getAction());
    }

}