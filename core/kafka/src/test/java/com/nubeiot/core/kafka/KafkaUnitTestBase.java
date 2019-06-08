package com.nubeiot.core.kafka;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.apache.kafka.clients.CommonClientConfigs;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.skyscreamer.jsonassert.Customization;
import org.slf4j.LoggerFactory;

import io.debezium.kafka.KafkaCluster;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class KafkaUnitTestBase {

    static final Customization IGNORE_TIMESTAMP = new Customization("data.timestamp", (o1, o2) -> true);
    static final Customization IGNORE_CHECKSUM = new Customization("data.checksum", (o1, o2) -> true);
    static final Customization IGNORE_HEADERS = new Customization("data.headers", (o1, o2) -> true);
    static final Customization IGNORE_EPOCH = new Customization("data.leaderEpoch", (o1, o2) -> true);

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();
    static final int TEST_TIMEOUT_SEC = 8;
    protected static KafkaCluster kafkaCluster;
    protected static int kafkaPort;

    protected Vertx vertx;
    protected KafkaConfig kafkaConfig;

    public static final EventModel KAFKA_PUBLISHER = EventModel.builder()
                                                               .address("kafka.broadcaster")
                                                               .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                               .event(EventAction.CREATE)
                                                               .build();

    @BeforeClass
    public static void setUp() throws IOException {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("org.apache.zookeeper")).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger("kafka")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("org.apache.kafka")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("org.apache.kafka.clients")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.DEBUG);
    }

    @AfterClass
    public static void tearDown() {
        if (Objects.nonNull(kafkaCluster)) {
            kafkaCluster.shutdown();
            kafkaCluster = null;
        }
    }

    static void kafkaCluster() throws IOException {
        kafkaCluster(tempFolder);
    }

    static void kafkaCluster(TemporaryFolder tempFolder) throws IOException {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        File dataDir = tempFolder.newFolder("kafka");
        kafkaPort = TestHelper.getRandomPort();
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(TestHelper.getRandomPort(), kafkaPort)
                                         .deleteDataPriorToStartup(true)
                                         .deleteDataUponShutdown(true)
                                         .addBrokers(1)
                                         .startup();
    }

    static KafkaConfig createKafkaConfig() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.getSecurityConfig().put("security.protocol", "PLAINTEXT");
        kafkaConfig.getClientConfig().put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + kafkaPort);
        return kafkaConfig;
    }

    @Before
    public void before(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        kafkaConfig = createKafkaConfig();
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    KafkaUnit startKafkaUnit(TestContext context, KafkaRouter router) {
        return startKafkaUnit(vertx, context, kafkaConfig, router);
    }

    private static KafkaUnit startKafkaUnit(Vertx vertx, TestContext context, KafkaConfig config, KafkaRouter router) {
        DeploymentOptions options = new DeploymentOptions().setConfig(config.toJson());
        String sharedKey = KafkaUnit.class.getName();
        KafkaUnit verticle = (KafkaUnit) new KafkaUnit(router).registerSharedData(sharedKey);
        vertx.sharedData().getLocalMap(sharedKey).put(SharedDataDelegate.SHARED_EVENTBUS, EventController.getInstance(vertx));
        return VertxHelper.deploy(vertx, context, options, verticle, TEST_TIMEOUT_SEC);
    }

}
