package com.nubeiot.core.http;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.mock.MockWebsocketEvent;

public class WebsocketEventBuilderTest {

    @Test(expected = InitializerError.class)
    public void test_no_register_metadata() {
        new WebsocketEventBuilder().validate();
    }

    @Test(expected = NullPointerException.class)
    public void test_register_null() {
        new WebsocketEventBuilder().register((WebsocketEventMetadata) null);
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
        WebsocketEventMetadata metadata = WebsocketEventMetadata.builder()
                                                                .listener(MockWebsocketEvent.SERVER_LISTENER)
                                                                .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                .path("xy")
                                                                .build();
        Assert.assertEquals(1, new WebsocketEventBuilder().register(metadata).validate().size());
    }

    @Test
    public void test_register_many_metadata_with_same_path() {
        WebsocketEventMetadata metadata1 = WebsocketEventMetadata.builder()
                                                                 .listener(MockWebsocketEvent.SERVER_LISTENER)
                                                                 .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                 .path("xy")
                                                                 .build();
        WebsocketEventMetadata metadata2 = WebsocketEventMetadata.builder()
                                                                 .listener(MockWebsocketEvent.SERVER_LISTENER)
                                                                 .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                 .path("xy")
                                                                 .build();
        Assert.assertEquals(1, new WebsocketEventBuilder().register(metadata1, metadata2).validate().size());
    }

    @Test
    public void test_register_many_metadata_with_different_path() {
        WebsocketEventMetadata metadata1 = WebsocketEventMetadata.builder()
                                                                 .listener(MockWebsocketEvent.SERVER_LISTENER)
                                                                 .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                 .path("xy")
                                                                 .build();
        WebsocketEventMetadata metadata2 = WebsocketEventMetadata.builder()
                                                                 .listener(MockWebsocketEvent.SERVER_PUBLISHER)
                                                                 .processor(MockWebsocketEvent.SERVER_PROCESSOR)
                                                                 .path("abc")
                                                                 .build();
        Assert.assertEquals(2, new WebsocketEventBuilder().register(metadata1, metadata2).validate().size());
    }

}