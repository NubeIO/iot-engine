package com.nubeiot.core.http.ws;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;
import com.nubeiot.core.http.mock.MockWebsocketEvent;

public class WebsocketServerEventMetadataTest {

    @Test(expected = InitializerError.class)
    public void test_no_addresses() {
        WebsocketServerEventMetadata.create("xy", null);
    }

    @Test
    public void test_register_with_no_path_no_publisher() {
        WebsocketServerEventMetadata metadata = WebsocketServerEventMetadata.create(MockWebsocketEvent.SERVER_LISTENER,
                                                                                    MockWebsocketEvent.SERVER_PROCESSOR);
        Assert.assertEquals("/", metadata.getPath());
        Assert.assertEquals(MockWebsocketEvent.SERVER_LISTENER, metadata.getListener());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PROCESSOR, metadata.getProcessor());
        Assert.assertNull(metadata.getPublisher());
    }

    @Test
    public void test_register_with_path_and_full_event() {
        WebsocketServerEventMetadata metadata = WebsocketServerEventMetadata.create("xy",
                                                                                    MockWebsocketEvent.SERVER_LISTENER,
                                                                                    MockWebsocketEvent.SERVER_PROCESSOR,
                                                                                    MockWebsocketEvent.SERVER_PUBLISHER);
        Assert.assertEquals("/xy", metadata.getPath());
        Assert.assertEquals(MockWebsocketEvent.SERVER_LISTENER, metadata.getListener());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PROCESSOR, metadata.getProcessor());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PUBLISHER, metadata.getPublisher());
    }

    @Test
    public void test_register_with_path_and_no_publisher() {
        WebsocketServerEventMetadata metadata = WebsocketServerEventMetadata.create("ab",
                                                                                    MockWebsocketEvent.SERVER_PUBLISHER);
        Assert.assertEquals("/ab", metadata.getPath());
        Assert.assertEquals(MockWebsocketEvent.SERVER_PUBLISHER, metadata.getPublisher());
        Assert.assertNull(metadata.getListener());
        Assert.assertNull(metadata.getProcessor());
    }

    @Test(expected = InitializerError.class)
    public void test_register_listener_invalid_pattern() {
        WebsocketServerEventMetadata.create(
                EventModel.clone(MockWebsocketEvent.SERVER_LISTENER, "invalid", EventPattern.PUBLISH_SUBSCRIBE),
                MockWebsocketEvent.SERVER_PROCESSOR);
    }

}
