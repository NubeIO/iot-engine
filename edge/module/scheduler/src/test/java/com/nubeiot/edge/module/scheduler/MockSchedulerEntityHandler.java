package com.nubeiot.edge.module.scheduler;

import java.util.Collection;
import java.util.Collections;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobTriggerDao;
import com.nubeiot.iotdata.scheduler.model.tables.daos.TriggerEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.scheduler.job.EventJobModel;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.trigger.CronTriggerModel;
import com.nubeiot.scheduler.trigger.PeriodicTriggerModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

import lombok.NonNull;

public class MockSchedulerEntityHandler extends SchedulerEntityHandler {

    public static final DeliveryEvent EVENT_1 = DeliveryEvent.builder()
                                                             .action(EventAction.CREATE)
                                                             .address("scheduler.1")
                                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                                             .build();
    public static final DeliveryEvent EVENT_2 = DeliveryEvent.builder()
                                                             .action(EventAction.PUBLISH)
                                                             .address("scheduler.2")
                                                             .pattern(EventPattern.POINT_2_POINT)
                                                             .build();
    public static final JobModel JOB_1 = EventJobModel.builder()
                                                      .group("group1")
                                                      .name("job1")
                                                      .forwardIfFailure(true)
                                                      .process(EVENT_1)
                                                      .build();
    public static final JobModel JOB_2 = EventJobModel.builder()
                                                      .group("group1")
                                                      .name("job2")
                                                      .forwardIfFailure(true)
                                                      .process(EVENT_1)
                                                      .callback(EVENT_2)
                                                      .build();
    public static final JobModel JOB_3 = EventJobModel.builder()
                                                      .group("group1")
                                                      .name("job3")
                                                      .forwardIfFailure(true)
                                                      .process(EVENT_1)
                                                      .callback(EVENT_2)
                                                      .build();
    public static final TriggerModel TRIGGER_1 = CronTriggerModel.builder()
                                                                 .group("group1")
                                                                 .name("trigger1")
                                                                 .expr("0 0 0 ? * SUN *")
                                                                 .tz("Australia/Sydney")
                                                                 .build();
    public static final TriggerModel TRIGGER_2 = CronTriggerModel.builder()
                                                                 .group("group1")
                                                                 .name("trigger2")
                                                                 .expr("1 1 1 ? * MON *")
                                                                 .tz("Australia/Sydney")
                                                                 .build();
    public static final TriggerModel TRIGGER_3 = PeriodicTriggerModel.builder()
                                                                     .group("group1")
                                                                     .name("trigger3")
                                                                     .intervalInSeconds(120)
                                                                     .repeat(10)
                                                                     .build();
    public static final TriggerModel TRIGGER_4 = CronTriggerModel.builder()
                                                                 .group("group2")
                                                                 .name("trigger4")
                                                                 .expr("0 0 0 ? * TUE *")
                                                                 .tz("Australia/Sydney")
                                                                 .build();

    public MockSchedulerEntityHandler(@NonNull Configuration jooqConfig, @NonNull Vertx vertx) {
        super(jooqConfig, vertx);
    }

    @Override
    public Single<EventMessage> initData() {
        eventClient().register("scheduler.1", new MockSchedulerListener());
        final JobEntityDao jobDao = dao(JobEntityDao.class);
        final TriggerEntityDao triggerDao = dao(TriggerEntityDao.class);
        final JobTriggerDao jobTriggerDao = dao(JobTriggerDao.class);
        return Single.concatArray(jobDao.insert(JobConverter.convert(JOB_1).setId(1)),
                                  jobDao.insert(JobConverter.convert(JOB_3).setId(2)),
                                  triggerDao.insert(TriggerConverter.convert(TRIGGER_1).setId(1)),
                                  triggerDao.insert(TriggerConverter.convert(TRIGGER_3).setId(2)),
                                  triggerDao.insert(TriggerConverter.convert(TRIGGER_4).setId(4)),
                                  jobTriggerDao.insert(new JobTrigger().setJobId(1).setTriggerId(1)),
                                  jobTriggerDao.insert(new JobTrigger().setJobId(1).setTriggerId(2).setEnabled(false)))
                     .reduce(0, Integer::sum)
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    public static class MockSchedulerListener implements EventListener {

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return Collections.singleton(EVENT_1.getAction());
        }

        @EventContractor(action = EventAction.CREATE)
        public JsonObject create() {
            return new JsonObject();
        }

    }

}
