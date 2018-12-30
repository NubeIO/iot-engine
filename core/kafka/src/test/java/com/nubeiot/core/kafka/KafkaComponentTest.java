package com.nubeiot.core.kafka;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.BaseHttpServerTest;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.mock.MockWebsocketEvent;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.http.ws.WebsocketEventMetadata;
import com.nubeiot.core.kafka.mock.MockKafkaConsumer;
import com.nubeiot.core.kafka.mock.MockKafkaProducer;

import io.debezium.kafka.KafkaCluster;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class KafkaComponentTest extends BaseHttpServerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private KafkaCluster kafkaCluster;
    private JsonObject producerConfig;
    private JsonObject consumerConfig;

    @BeforeClass
    public static void beforeSuite() {
        BaseHttpServerTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        this.httpConfig.put("enabled", false).put("__socket__", new JsonObject().put("enabled", true));
        File dataDir = folder.newFolder("cluster");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir)
                                         .withPorts(2182, 9093)
                                         .addBrokers(1)
                                         .deleteDataPriorToStartup(true)
                                         .startup();
        Properties props = kafkaCluster.useTo().getConsumerProperties("test", "consumer", OffsetResetStrategy.EARLIEST);
        Properties producerProps = kafkaCluster.useTo().getProducerProperties("producer");
        consumerConfig = new JsonObject((Map) props);
        producerConfig = new JsonObject((Map) producerProps);
    }

    @After
    public void after(TestContext context) {
        kafkaCluster.shutdown();
        super.after(context);
    }

    @Test
    public void test_client_consumer(TestContext context) {
        WebsocketEventMetadata metadata = MockWebsocketEvent.ONLY_PUBLISHER;
        startServer(new HttpServerRouter().registerEventBusSocket(metadata));
        new MockKafkaConsumer(vertx.getDelegate(), consumerConfig, "nube", metadata::getPublisher).start();
        new MockKafkaProducer(vertx.getDelegate(), producerConfig, "nube", supply()).start();
        Async async = context.async(1);
        setupConsumer(async, metadata.getPublisher().getAddress(),
                      o -> assertResponse(context, async, supply().get().toJson(), (JsonObject) o));
    }

    @Test
    public void test_web_consumer(TestContext context) throws InterruptedException {
        WebsocketEventMetadata metadata = MockWebsocketEvent.ONLY_PUBLISHER;
        JsonObject expected = createWebsocketMsg(metadata.getPublisher().getAddress(), supply().get(),
                                                 BridgeEventType.RECEIVE);
        startServer(new HttpServerRouter().registerEventBusSocket(metadata));
        new MockKafkaConsumer(vertx.getDelegate(), consumerConfig, "nube", metadata::getPublisher).start();
        new MockKafkaProducer(vertx.getDelegate(), producerConfig, "nube", supply()).start();
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(async, Urls.combinePath("ws", metadata.getPath()),
                                         clientRegister(metadata.getPublisher().getAddress()), context::fail);
        ws.handler(buffer -> assertResponse(context, async, expected, buffer));
    }

    private Supplier<EventMessage> supply() {
        return () -> EventMessage.success(EventAction.RETURN, new JsonObject().put("hello", "kafka"));
    }

}