package com.nubeiot.core.kafka;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
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
import org.slf4j.LoggerFactory;

import io.debezium.kafka.KafkaCluster;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.BaseHttpServerTest;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.mock.MockWebsocketEvent;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.http.ws.WebsocketEventMetadata;
import com.nubeiot.core.kafka.mock.MockKafkaConsumer;
import com.nubeiot.core.kafka.mock.MockKafkaProducer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public class KafkaComponentTest extends BaseHttpServerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private KafkaCluster kafkaCluster;
    private JsonObject producerConfig;
    private JsonObject consumerConfig;
    private MockKafkaConsumer consumer;
    private MockKafkaProducer producer;

    @BeforeClass
    public static void beforeSuite() {
        BaseHttpServerTest.beforeSuite();
        ((Logger) LoggerFactory.getLogger("kafka")).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger("org.apache.zookeeper")).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger("org.apache.kafka")).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger("org.apache.kafka.clients")).setLevel(Level.INFO);
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        this.httpConfig.setEnabled(false);
        this.httpConfig.getWebsocketCfg().setEnabled(true);
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
        setupConsumer(async, metadata.getPublisher().getAddress(),
                      o -> assertResponse(context, async, supply().get().toJson(), (JsonObject) o));
    }

    @Test
    public void test_web_consumer(TestContext context) throws InterruptedException {
        WebsocketEventMetadata metadata = MockWebsocketEvent.ONLY_PUBLISHER;
        JsonObject expected = createWebsocketMsg(metadata.getPublisher().getAddress(), supply().get(),
                                                 BridgeEventType.RECEIVE);
        startServer(context, new HttpServerRouter().registerEventBusSocket(metadata));
        startKafkaClient(metadata);
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(async, Urls.combinePath("ws", metadata.getPath()),
                                         clientRegister(metadata.getPublisher().getAddress()), context::fail);
        ws.handler(buffer -> assertResponse(context, async, expected, buffer));
    }

    private void startKafkaClient(WebsocketEventMetadata metadata) {
        consumer = new MockKafkaConsumer(vertx.getDelegate(), consumerConfig, "nube", metadata::getPublisher);
        producer = new MockKafkaProducer(vertx.getDelegate(), producerConfig, "nube", supply());
        consumer.start();
        producer.start();
    }

    private Supplier<EventMessage> supply() {
        return () -> EventMessage.success(EventAction.RETURN, new JsonObject().put("hello", "kafka"));
    }

}
