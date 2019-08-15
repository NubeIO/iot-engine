package com.nubeiot.edge.module.scheduler.service;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.scheduler.model.Tables;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.daos.TriggerEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.interfaces.IJobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.iotdata.scheduler.model.tables.records.JobEntityRecord;
import com.nubeiot.iotdata.scheduler.model.tables.records.TriggerEntityRecord;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

interface SchedulerMetadata {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class JobEntityMetadata implements SerialKeyEntity<JobEntity, JobEntityRecord, JobEntityDao> {

        static final JobEntityMetadata INSTANCE = new JobEntityMetadata();

        @Override
        public @NonNull Class<JobEntity> modelClass() {
            return JobEntity.class;
        }

        @Override
        public @NonNull Class<JobEntityDao> daoClass() {
            return JobEntityDao.class;
        }

        @Override
        public @NonNull JsonTable<JobEntityRecord> table() {
            return Tables.JOB_ENTITY;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "job_id";
        }

        @Override
        public @NonNull String listKey() {
            return "jobs";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TriggerEntityMetadata implements SerialKeyEntity<TriggerEntity, TriggerEntityRecord, TriggerEntityDao> {

        static final TriggerEntityMetadata INSTANCE = new TriggerEntityMetadata();

        @Override
        public @NonNull Class<TriggerEntity> modelClass() {
            return TriggerEntity.class;
        }

        @Override
        public @NonNull Class<TriggerEntityDao> daoClass() {
            return TriggerEntityDao.class;
        }

        @Override
        public @NonNull JsonTable<TriggerEntityRecord> table() {
            return Tables.TRIGGER_ENTITY;
        }

        @Override
        public @NonNull String requestKeyName() {
            return "trigger_id";
        }

        @Override
        public @NonNull String listKey() {
            return "triggers";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TriggerByJobMetadata extends JobTriggerSchedulerService.Metadata {

        static final TriggerByJobMetadata INSTANCE = new TriggerByJobMetadata();

        @Override
        public @NonNull String requestKeyName() {
            return "job_id";
        }

        @Override
        public @NonNull String jsonKeyName() {
            return "job_id";
        }

        @Override
        public @NonNull String listKey() {
            return "triggers";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class JobByTriggerMetadata extends JobTriggerSchedulerService.Metadata {

        static final JobByTriggerMetadata INSTANCE = new JobByTriggerMetadata();

        @Override
        public @NonNull String requestKeyName() {
            return "trigger_id";
        }

        @Override
        public @NonNull String jsonKeyName() {
            return "trigger_id";
        }

        @Override
        public @NonNull String listKey() {
            return "jobs";
        }

    }


    @Getter
    final class JobTriggerView extends JobTrigger {

        private JobEntity job;
        private TriggerEntity trigger;

        public JobTriggerView setJob(JobEntity job) {
            this.job = job;
            return this;
        }

        public JobTriggerView setTrigger(TriggerEntity trigger) {
            this.trigger = trigger;
            return this;
        }

        @Override
        public IJobTrigger fromJson(JsonObject json) {
            final IJobTrigger jobTrigger = super.fromJson(json);
            final JsonObject job = JsonData.safeGet(json, "job", JsonObject.class, new JsonObject());
            final JsonObject trigger = JsonData.safeGet(json, "trigger", JsonObject.class, new JsonObject());
            if (job.isEmpty() && trigger.isEmpty()) {
                return jobTrigger;
            }
            final JobTriggerView view = (JobTriggerView) jobTrigger;
            return view.setJob(new JobEntity(job)).setTrigger(new TriggerEntity(trigger));
        }

        @Override
        public JsonObject toJson() {
            return super.toJson()
                        .put("job", Objects.isNull(job) ? null : job.toJson())
                        .put("trigger", Objects.isNull(trigger) ? null : trigger.toJson());
        }

    }

}
