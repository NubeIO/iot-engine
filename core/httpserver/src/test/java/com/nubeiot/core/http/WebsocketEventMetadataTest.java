package com.nubeiot.core.http;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.mock.MockWebsocketEvent;

public class WebsocketEventMetadataTest {

    @Test(expected = InitializerError.class)
    public void test_no_addresses() {
        WebsocketEventMetadata.builder().path("xy").build();
    }

    @Test
    public void test_register_with_no_path() {
        WebsocketEventMetadata metadata = WebsocketEventMetadata.builder()
                                                                .listener(MockWebsocketEvent.SERVER_LISTENER)
                                                                .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                .build();
        Assert.assertEquals("/", metadata.getPath());
        Assert.assertEquals(MockWebsocketEvent.SERVER_LISTENER, metadata.getListener());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PROCESSOR, metadata.getProcessor());
        Assert.assertEquals(MockWebsocketEvent.SERVER_LISTENER, metadata.getPublisher());
    }

    @Test
    public void test_register_with_path_and_full_event() {
        WebsocketEventMetadata metadata = WebsocketEventMetadata.builder()
                                                                .listener(MockWebsocketEvent.SERVER_LISTENER)
                                                                .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                .publisher(MockWebsocketEvent.SERVER_PUBLISHER)
                                                                .path("xy")
                                                                .build();
        Assert.assertEquals("/xy", metadata.getPath());
        Assert.assertEquals(MockWebsocketEvent.SERVER_LISTENER, metadata.getListener());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PROCESSOR, metadata.getProcessor());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PUBLISHER, metadata.getPublisher());
    }

    @Test
    public void test_register_with_path_and_no_publisher() {
        WebsocketEventMetadata metadata = WebsocketEventMetadata.builder()
                                                                .listener(MockWebsocketEvent.SERVER_LISTENER)
                                                                .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                .path("ab")
                                                                .build();
        Assert.assertEquals("/ab", metadata.getPath());
        Assert.assertEquals(MockWebsocketEvent.SERVER_LISTENER, metadata.getListener());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PROCESSOR, metadata.getProcessor());
        Assert.assertNotNull(metadata.getPublisher());
        Assert.assertEquals(MockWebsocketEvent.SERVER_LISTENER, metadata.getPublisher());
    }

}