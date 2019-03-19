package com.nubeiot.core.kafka;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.HttpServerTestBase;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.mock.MockWebsocketEvent;
import com.nubeiot.core.http.ws.WebsocketEventMetadata;
import com.nubeiot.core.kafka.mock.MockKafkaConsumer;
import com.nubeiot.core.kafka.mock.MockKafkaProducer;

@RunWith(VertxUnitRunner.class)
public class KafkaWebsocketTest extends HttpServerTestBase {

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();
    private KafkaConfig kafkaConfig;
    private MockKafkaConsumer consumer;
    private MockKafkaProducer producer;

    @BeforeClass
    public static void beforeSuite() throws IOException {
        TestHelper.setup();
        KafkaUnitTestBase.setUp();
        KafkaUnitTestBase.kafkaCluster(tempFolder);
    }

    @AfterClass
    public static void tearDown() {
        KafkaUnitTestBase.tearDown();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        this.enableWebsocket();
        this.kafkaConfig = KafkaUnitTestBase.createKafkaConfig();
    }

    @After
    public void after(TestContext context) {
        if (Objects.nonNull(consumer)) {
            consumer.stop();
        }
        if (Objects.nonNull(producer)) {
            producer.stop();
        }
        super.after(context);
    }

    @Test
    public void test_client_consumer(TestContext context) {
        WebsocketEventMetadata metadata = MockWebsocketEvent.ONLY_PUBLISHER;
        startServer(context, new HttpServerRouter().registerEventBusSocket(metadata));
        startKafkaClient(metadata);
        Async async = context.async(1);
        assertConsumerData(async, metadata.getPublisher().getAddress(),
                           o -> JsonHelper.assertJson(context, async, supply().get().toJson(), (JsonObject) o));
    }

    @Test
    public void test_web_consumer(TestContext context) throws InterruptedException {
        WebsocketEventMetadata metadata = MockWebsocketEvent.ONLY_PUBLISHER;
        JsonObject expected = createWebsocketMsg(metadata.getPublisher().getAddress(), supply().get(),
                                                 BridgeEventType.RECEIVE);
        startServer(context, new HttpServerRouter().registerEventBusSocket(metadata));
        startKafkaClient(metadata);
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(context, async, Urls.combinePath("ws", metadata.getPath()),
                                         clientRegister(metadata.getPublisher().getAddress()), context::fail);
        ws.handler(buffer -> assertResponse(context, async, expected, buffer));
    }

    private void startKafkaClient(WebsocketEventMetadata metadata) {
        consumer = new MockKafkaConsumer(vertx.getDelegate(), kafkaConfig.getConsumerConfig(), "nube",
                                         metadata::getPublisher);
        producer = new MockKafkaProducer(vertx.getDelegate(), kafkaConfig.getProducerConfig(), "nube", supply());
        consumer.start();
        producer.start();
    }

    private Supplier<EventMessage> supply() {
        return () -> EventMessage.success(EventAction.RETURN, new JsonObject().put("hello", "kafka"));
    }

}
