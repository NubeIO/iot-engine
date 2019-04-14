package com.nubeiot.core.http.ws;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;
import com.nubeiot.core.http.mock.MockWebsocketEvent;

public class WebsocketEventBuilderTest {

    @Test(expected = InitializerError.class)
    public void test_no_register_metadata() {
        new WebsocketEventBuilder().validate();
    }

    @Test(expected = NullPointerException.class)
    public void test_register_null() {
        new WebsocketEventBuilder().register((WebsocketServerEventMetadata) null);
    }

    @Test
    public void test_customize_root() {
        WebsocketEventBuilder builder = new WebsocketEventBuilder();
        Assert.assertEquals("/ws", builder.getRootWs());
        builder.rootWs("rtc");
        Assert.assertEquals("/rtc", builder.getRootWs());
    }

    @Test
    public void test_one_metadata() {
        WebsocketServerEventMetadata metadata = WebsocketServerEventMetadata.create(MockWebsocketEvent.SERVER_LISTENER,
                                                                                    MockWebsocketEvent.SERVER_PROCESSOR);
        Assert.assertEquals(1, new WebsocketEventBuilder().register(metadata).validate().size());
    }

    @Test
    public void test_register_many_metadata_with_same_path() {
        WebsocketServerEventMetadata metadata1 = WebsocketServerEventMetadata.create("xy",
                                                                                     MockWebsocketEvent.SERVER_LISTENER,
                                                                                     MockWebsocketEvent.SERVER_PROCESSOR);
        WebsocketServerEventMetadata metadata2 = WebsocketServerEventMetadata.create("xy",
                                                                                     MockWebsocketEvent.SERVER_LISTENER,
                                                                                     MockWebsocketEvent.SERVER_PROCESSOR);
        Assert.assertEquals(1, new WebsocketEventBuilder().register(metadata1, metadata2).validate().size());
    }

    @Test
    public void test_register_many_metadata_with_different_path() {
        WebsocketServerEventMetadata metadata1 = WebsocketServerEventMetadata.create("xy",
                                                                                     MockWebsocketEvent.SERVER_LISTENER,
                                                                                     MockWebsocketEvent.SERVER_PROCESSOR);
        WebsocketServerEventMetadata metadata2 = WebsocketServerEventMetadata.create("abc",
                                                                                     MockWebsocketEvent.SERVER_LISTENER,
                                                                                     MockWebsocketEvent.SERVER_PROCESSOR);
        Assert.assertEquals(2, new WebsocketEventBuilder().register(metadata1, metadata2).validate().size());
    }

}
