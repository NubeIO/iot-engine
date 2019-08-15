package com.nubeiot.edge.module.scheduler.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ManyToOneReferenceEntityService;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.module.scheduler.service.SchedulerMetadata.JobTriggerView;
import com.nubeiot.iotdata.scheduler.model.Tables;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobTriggerDao;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.records.JobTriggerRecord;
import com.nubeiot.scheduler.QuartzSchedulerContext;

import lombok.NonNull;

abstract class JobTriggerSchedulerService<M extends EntityMetadata<Integer, JobTrigger, JobTriggerRecord,
                                                                      JobTriggerDao>>
    extends AbstractSchedulerService<Integer, JobTrigger, JobTriggerRecord, JobTriggerDao, M>
    implements ManyToOneReferenceEntityService<Integer, JobTrigger, JobTriggerRecord, JobTriggerDao, M> {

    JobTriggerSchedulerService(@NonNull EntityHandler entityHandler, @NonNull QuartzSchedulerContext schedulerContext) {
        super(entityHandler, schedulerContext);
    }

    @Override
    public final Map<String, Function<String, ?>> jsonFieldConverter() {
        return Collections.singletonMap(secondaryParam(), Functions.toInt());
    }

    @Override
    public final Set<EventMethodDefinition> definitions() {
        return Collections.singleton(EventMethodDefinition.createDefault(servicePath(), "/:" + secondaryParam()));
    }

    abstract String secondaryParam();

    abstract String servicePath();

    @Override
    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData requestData) {
        return ManyToOneReferenceEntityService.super.list(requestData);
    }

    @Override
    @EventContractor(action = EventAction.GET_ONE, returnType = Single.class)
    public Single<JsonObject> get(RequestData requestData) {
        return ManyToOneReferenceEntityService.super.get(requestData);
    }

    @Override
    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> create(RequestData requestData) {
        return super.create(requestData);
    }

    @Override
    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData requestData) {
        return super.update(requestData);
    }

    @Override
    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patch(RequestData requestData) {
        return super.patch(requestData);
    }

    @Override
    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData requestData) {
        return super.delete(requestData);
    }

    static abstract class Metadata implements SerialKeyEntity<JobTrigger, JobTriggerRecord, JobTriggerDao> {

        @Override
        public final @NonNull Class<? extends JobTrigger> modelClass() {
            return JobTriggerView.class;
        }

        @Override
        public final @NonNull Class<JobTriggerDao> daoClass() {
            return JobTriggerDao.class;
        }

        @Override
        public final @NonNull JsonTable<JobTriggerRecord> table() {
            return Tables.JOB_TRIGGER;
        }

    }

}
