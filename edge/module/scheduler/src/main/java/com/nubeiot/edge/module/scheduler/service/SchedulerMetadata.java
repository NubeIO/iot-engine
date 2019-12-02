package com.nubeiot.edge.module.scheduler.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jooq.OrderField;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.CompositeMetadata.AbstractCompositeMetadata;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.MetadataIndex;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.Tables;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobTriggerDao;
import com.nubeiot.iotdata.scheduler.model.tables.daos.TriggerEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.iotdata.scheduler.model.tables.records.JobEntityRecord;
import com.nubeiot.iotdata.scheduler.model.tables.records.JobTriggerRecord;
import com.nubeiot.iotdata.scheduler.model.tables.records.TriggerEntityRecord;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public interface SchedulerMetadata extends MetadataIndex {

    List<EntityMetadata> INDEX = Collections.unmodifiableList(MetadataIndex.find(SchedulerMetadata.class));

    @Override
    default List<EntityMetadata> index() {
        return INDEX;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class JobEntityMetadata implements SerialKeyEntity<JobEntity, JobEntityRecord, JobEntityDao> {

        public static final JobEntityMetadata INSTANCE = new JobEntityMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.scheduler.model.tables.JobEntity table() {
            return Tables.JOB_ENTITY;
        }

        @Override
        public @NonNull Class<JobEntity> modelClass() {
            return JobEntity.class;
        }

        @Override
        public @NonNull Class<JobEntityDao> daoClass() {
            return JobEntityDao.class;
        }

        @Override
        public JobEntity parseFromRequest(@NonNull JsonObject request) throws IllegalArgumentException {
            return JobConverter.convert(request);
        }

        @Override
        public @NonNull String requestKeyName() { return "job_id"; }

        @Override
        public @NonNull String singularKeyName() { return "job"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().GROUP.asc(), table().NAME.asc());
        }

        @Override
        public JobEntity onUpdating(@NonNull JobEntity dbData, RequestData reqData) throws IllegalArgumentException {
            final JsonObject body = reqData.body().copy();
            body.remove(requestKeyName());
            return JobConverter.convert(body).setId(dbData.getId());
        }

        @Override
        public JobEntity onPatching(@NonNull JobEntity dbData, RequestData reqData) throws IllegalArgumentException {
            final JobModel job = JobConverter.convert(dbData);
            final JsonObject body = reqData.body().copy();
            body.remove(requestKeyName());
            return parseFromEntity(JsonPojo.merge(dbData, JobConverter.convert(job.toJson().mergeIn(body))));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TriggerEntityMetadata implements SerialKeyEntity<TriggerEntity, TriggerEntityRecord, TriggerEntityDao> {

        public static final TriggerEntityMetadata INSTANCE = new TriggerEntityMetadata();

        @Override
        public @NonNull com.nubeiot.iotdata.scheduler.model.tables.TriggerEntity table() {
            return Tables.TRIGGER_ENTITY;
        }

        @Override
        public @NonNull Class<TriggerEntity> modelClass() {
            return TriggerEntity.class;
        }

        @Override
        public @NonNull Class<TriggerEntityDao> daoClass() {
            return TriggerEntityDao.class;
        }

        @Override
        public TriggerEntity parseFromRequest(@NonNull JsonObject request) throws IllegalArgumentException {
            return TriggerConverter.convert(request);
        }

        @Override
        public @NonNull String requestKeyName() { return "trigger_id"; }

        @Override
        public @NonNull String singularKeyName() { return "trigger"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().GROUP.asc(), table().NAME.asc());
        }

        @Override
        public TriggerEntity onUpdating(@NonNull TriggerEntity dbData, RequestData reqData)
            throws IllegalArgumentException {
            final JsonObject body = reqData.body().copy();
            body.remove(requestKeyName());
            return TriggerConverter.convert(body).setId(dbData.getId());
        }

        @Override
        public TriggerEntity onPatching(@NonNull TriggerEntity dbData, RequestData reqData)
            throws IllegalArgumentException {
            final TriggerModel trigger = TriggerConverter.convert(dbData);
            final JsonObject body = reqData.body().copy();
            body.remove(requestKeyName());
            return parseFromEntity(JsonPojo.merge(dbData, TriggerConverter.convert(trigger.toJson().mergeIn(body))));
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class JobTriggerMetadata
        extends AbstractCompositeMetadata<Integer, JobTrigger, JobTriggerRecord, JobTriggerDao, JobTriggerComposite>
        implements SerialKeyEntity<JobTrigger, JobTriggerRecord, JobTriggerDao> {

        public static final JobTriggerMetadata INSTANCE = new JobTriggerMetadata().addSubItem(
            JobEntityMetadata.INSTANCE, TriggerEntityMetadata.INSTANCE);

        @Override
        public final @NonNull Class<JobTriggerComposite> modelClass() { return JobTriggerComposite.class; }

        @Override
        public final @NonNull com.nubeiot.iotdata.scheduler.model.tables.JobTrigger table() { return Tables.JOB_TRIGGER; }

        @Override
        public final @NonNull Class<JobTriggerDao> daoClass() { return JobTriggerDao.class; }

        @Override
        public @NonNull String singularKeyName() {
            return "job_trigger";
        }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().ENABLED.desc(), table().TRIGGER_ID.asc(), table().JOB_ID.asc());
        }

        @Override
        public @NonNull Class<JobTrigger> rawClass() {
            return JobTrigger.class;
        }

    }

}
