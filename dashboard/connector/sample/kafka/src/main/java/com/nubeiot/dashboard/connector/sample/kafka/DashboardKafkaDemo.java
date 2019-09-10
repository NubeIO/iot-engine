package com.nubeiot.dashboard.connector.sample.kafka;

import java.util.UUID;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.HttpServerProvider;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;
import com.nubeiot.core.kafka.KafkaContext;
import com.nubeiot.core.kafka.KafkaEventMetadata;
import com.nubeiot.core.kafka.KafkaRouter;
import com.nubeiot.core.kafka.KafkaUnitProvider;
import com.nubeiot.core.kafka.service.KafkaConsumerService;

public final class DashboardKafkaDemo extends ContainerVerticle {

    public static final EventModel KAFKA_EB_PUBLISHER = EventModel.builder()
                                                                  .address("edge.kafka.data")
                                                                  .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                                  .local(false)
                                                                  .event(EventAction.GET_ONE)
                                                                  .build();
    public static final EventModel KAFKA_ENABLED = EventModel.builder()
                                                             .address("edge.kafka.manage.enable")
                                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                                             .event(EventAction.UPDATE)
                                                             .local(true)
                                                             .build();

    private KafkaConsumerService consumerService;

    @Override
    public void start() {
        logger.info("DASHBOARD KAFKA DEMO");
        super.start();
        this.addProvider(new HttpServerProvider(initHttpRouter()))
            .addProvider(new KafkaUnitProvider(initKafkaRouter()), this::startConsumer);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(KAFKA_ENABLED, new EnableKafkaEventListener());
    }

    private KafkaRouter initKafkaRouter() {
        return new KafkaRouter().registerKafkaEvent(KafkaEventMetadata.consumer()
                                                                      .model(KAFKA_EB_PUBLISHER)
                                                                      .topic("GPIO")
                                                                      .keyClass(String.class)
                                                                      .valueClass(UUID.class)
                                                                      .build());
    }

    private HttpServerRouter initHttpRouter() {
        return new HttpServerRouter().registerEventBusApi(EnableKafkaProducerApi.class)
                                     .registerEventBusSocket(WebsocketServerEventMetadata.create(KAFKA_EB_PUBLISHER));
    }

    private void startConsumer(KafkaContext context) {
        consumerService = context.getConsumerService();
    }

}
