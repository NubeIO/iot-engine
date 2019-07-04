package com.nubeiot.scheduler;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.scheduler.job.EventJobModel;

import lombok.NonNull;

public class MockEventScheduler {

    public static final EventModel PROCESS_EVENT = EventModel.builder()
                                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                                             .addEvents(EventAction.CREATE)
                                                             .local(true)
                                                             .address("event.job.model.test")
                                                             .build();
    public static final EventModel CALLBACK_EVENT = EventModel.builder()
                                                              .pattern(EventPattern.PUBLISH_SUBSCRIBE)
                                                              .addEvents(EventAction.PUBLISH)
                                                              .local(true)
                                                              .address("event.job.model.callback.test")
                                                              .build();


    public static class MockProcessEventSchedulerListener implements EventListener {

        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return PROCESS_EVENT.getEvents();
        }

        @EventContractor(action = EventAction.CREATE)
        public JsonObject increaseNumber() {
            return new JsonObject().put("count", count.getAndIncrement());
        }

    }


    public static class MockJobModel {

        public static EventJobModel create(String name) {
            return EventJobModel.builder()
                                .name(name)
                                .process(DeliveryEvent.from(PROCESS_EVENT, EventAction.CREATE))
                                .callback(DeliveryEvent.from(CALLBACK_EVENT, EventAction.PUBLISH))
                                .build();
        }

    }

}
