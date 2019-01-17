package com.nubeiot.core.kafka;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.supplier.KafkaConsumerProvider;
import com.nubeiot.core.kafka.supplier.KafkaProducerSupplier;
import com.nubeiot.core.utils.Configs;

@RunWith(VertxUnitRunner.class)
public class KafkaUnitTest {

    protected static final int TEST_TIMEOUT = 3000;
    protected Vertx vertx;
    protected KafkaConfig kafkaConfig;

    @Before
    public void before(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        kafkaConfig = new KafkaConfig();
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    protected void startKafka(TestContext context, KafkaRouter kafkaRouter) {
        DeploymentOptions options = new DeploymentOptions().setConfig(kafkaConfig.toJson());
        CountDownLatch latch = new CountDownLatch(1);
        vertx.deployVerticle(new KafkaUnit(kafkaRouter), options, context.asyncAssertSuccess(id -> latch.countDown()));
        try {
            latch.await(TEST_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            context.fail(e);
        }
    }

    protected void startKafka(TestContext context, KafkaRouter kafkaRouter, Consumer<Throwable> consumer) {
        DeploymentOptions options = new DeploymentOptions().setConfig(kafkaConfig.toJson());
        vertx.deployVerticle(new KafkaUnit(kafkaRouter), options, context.asyncAssertFailure(consumer::accept));
    }

    @Test
    public void test_startup_success(TestContext context) {
        //        startKafka(context, new KafkaRouter());
    }

    @Test
    public void test_can_create_consumer() {
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        KafkaConsumer<String, EventMessage> consumer = KafkaConsumerProvider.create(vertx.getDelegate(),
                                                                                    from.getConsumerConfig(),
                                                                                    String.class, EventMessage.class);
        consumer.close();
    }

    @Test
    public void test_can_create_producer() {
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        KafkaProducer<String, EventMessage> producer = KafkaProducerSupplier.create(vertx.getDelegate(),
                                                                                    from.getProducerConfig(),
                                                                                    String.class, EventMessage.class);
        producer.close();
    }

}
