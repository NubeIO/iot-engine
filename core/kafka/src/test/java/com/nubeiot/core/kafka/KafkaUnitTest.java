package com.nubeiot.core.kafka;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.handler.KafkaErrorHandler;
import com.nubeiot.core.kafka.handler.KafkaRecord;
import com.nubeiot.core.kafka.handler.producer.KafkaProducerHandler;
import com.nubeiot.core.kafka.mock.TestErrorHandler;
import com.nubeiot.core.kafka.mock.TestProducerHandler;
import com.nubeiot.core.utils.DateTimes;

@RunWith(VertxUnitRunner.class)
public class KafkaUnitTest extends KafkaUnitTestBase {

    @Rule
    public Timeout timeout = Timeout.seconds(TEST_TIMEOUT_SEC);

    @BeforeClass
    public static void setUp() throws IOException {
        KafkaUnitTestBase.setUp();
        KafkaUnitTestBase.kafkaCluster();
    }

    @Test
    public void test_startup_with_no_router(TestContext context) {
        startKafkaUnit(context, new KafkaRouter());
    }

    @Test
    public void test_producer_can_send(TestContext context) {
        String topic = UUID.randomUUID().toString();
        Async async = context.async(1);
        KafkaRouter router = createProducerRouter(context, async, topic, 0);
        startKafkaUnit(context, router).getContext().getProducerService().publish(topic, 0, "test", topic);
    }

    @Test
    public void test_consumer_can_read(TestContext context) {
        String topic = UUID.randomUUID().toString();
        Async async = context.async(2);
        JsonObject expected = KafkaRecord.serialize(
            new ConsumerRecord<>(topic, 0, 0, DateTimes.nowMilli(), TimestampType.CREATE_TIME, -1, -1, -1, "test",
                                 topic)).toJson(KafkaRecord.NO_HEADERS_MAPPER);
        setupConsumer(async, KAFKA_PUBLISHER.getAddress(), o -> {
            EventMessage message = EventMessage.initial(EventAction.CREATE, expected);
            assertResponse(context, async, message.toJson(), (JsonObject) o);
        });
        KafkaEventMetadata consumerEvent = KafkaEventMetadata.consumer()
                                                             .model(KAFKA_PUBLISHER)
                                                             .topic(topic)
                                                             .keyClass(String.class)
                                                             .valueClass(String.class)
                                                             .build();
        KafkaRouter router = createProducerRouter(context, async, topic, 0).registerKafkaEvent(consumerEvent);
        router.addConsumerErrorHandler(consumerEvent.getTechId(), errorHandler(context, async));
        KafkaUnit unit = startKafkaUnit(context, router);
        unit.getContext().getProducerService().publish(topic, 0, "test", topic);
    }

    protected void setupConsumer(Async async, String address, Consumer<Object> assertOut) {
        MessageConsumer<Object> consumer = vertx.eventBus().consumer(address);
        consumer.handler(event -> {
            System.out.println("Received message from address: " + address);
            EventMessage msg = EventMessage.from(event.body());
            Assert.assertNotNull(msg.getData());
            Assert.assertNotEquals(-1, msg.getData().getValue("timestamp"));
            Assert.assertNotEquals(-1, msg.getData().getValue("checksum"));
            assertOut.accept(event.body());
            consumer.unregister(v -> TestHelper.testComplete(async));
        });
    }

    private static void assertResponse(TestContext context, Async async, JsonObject expected, JsonObject actual) {
        JsonHelper.assertJson(context, async, expected, actual, IGNORE_TIMESTAMP, IGNORE_CHECKSUM, IGNORE_EPOCH);
    }

    private KafkaErrorHandler errorHandler(TestContext context, Async async) {
        return new TestErrorHandler<>(context, async, TestHelper::testComplete);
    }

    private KafkaRouter createProducerRouter(TestContext context, Async async, String topic, Integer partition) {
        KafkaProducerHandler handler = TestProducerHandler.builder()
                                                          .context(context)
                                                          .async(async)
                                                          .countdown(TestHelper::testComplete)
                                                          .topic(topic)
                                                          .partition(partition)
                                                          .build();
        KafkaEventMetadata metadata = KafkaEventMetadata.producer()
                                                        .handler(handler)
                                                        .topic(topic)
                                                        .keyClass(String.class)
                                                        .valueClass(String.class)
                                                        .build();
        return new KafkaRouter().registerKafkaEvent(metadata)
                                .addProducerErrorHandler(metadata.getTechId(), errorHandler(context, async));
    }

}
