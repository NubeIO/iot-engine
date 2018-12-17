package com.nubeiot.core.http;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.event.EventType;

import io.vertx.core.http.HttpMethod;

public class EventBusRestMetadataTest {

    private EventBusRestMetadata createMetadata(EventType event, HttpMethod method) {
        return EventBusRestMetadata.builder()
                                   .action(event)
                                   .address("address.1")
                                   .method(method)
                                   .path("/api/gold")
                                   .paramName("gold_id")
                                   .build();
    }

    @Test
    public void test_post() {
        EventBusRestMetadata metadata = createMetadata(EventType.CREATE, HttpMethod.POST);
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_list() {
        EventBusRestMetadata metadata = createMetadata(EventType.GET_LIST, HttpMethod.GET);
        Assert.assertEquals("/api/golds", metadata.getPath());
    }

    @Test
    public void test_get_one() {
        EventBusRestMetadata metadata = createMetadata(EventType.GET_ONE, HttpMethod.GET);
        Assert.assertEquals("/api/gold/:gold_id", metadata.getPath());
    }

    @Test
    public void test_other_method() {
        EventBusRestMetadata metadata = createMetadata(EventType.INIT, HttpMethod.OPTIONS);
        Assert.assertEquals("/api/gold", metadata.getPath());
        Assert.assertEquals(HttpMethod.OPTIONS, metadata.getMethod());
    }

    @Test
    public void test_custom_gen_path() {
        EventBusRestMetadata metadata = EventBusRestMetadata.builder()
                                                            .action(EventType.GET_ONE)
                                                            .address("address.1")
                                                            .method(HttpMethod.POST)
                                                            .path("/api/translate")
                                                            .generatePath(EventBusRestMetadata::rawPath)
                                                            .build();
        Assert.assertEquals("/api/translate", metadata.getPath());
        Assert.assertEquals(HttpMethod.POST, metadata.getMethod());
        Assert.assertEquals(EventType.GET_ONE, metadata.getAction());
    }

}