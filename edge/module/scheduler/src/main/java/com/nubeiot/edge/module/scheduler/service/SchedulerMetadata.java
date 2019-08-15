package com.nubeiot.edge.module.scheduler.service;

import java.util.HashMap;
import java.util.Map;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.CompositePojo;
import com.nubeiot.core.sql.EntityMetadata.SerialKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.scheduler.model.Tables;
import com.nubeiot.iotdata.scheduler.model.tables.daos.JobEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.daos.TriggerEntityDao;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.iotdata.scheduler.model.tables.records.JobEntityRecord;
import com.nubeiot.iotdata.scheduler.model.tables.records.TriggerEntityRecord;

import lombok.AccessLevel;
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
        public @NonNull String requestKeyName() { return "job_id"; }

        @Override
        public @NonNull String singularKeyName() { return "job"; }

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
        public @NonNull String requestKeyName() { return "trigger_id"; }

        @Override
        public @NonNull String singularKeyName() { return "trigger"; }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TriggerByJobMetadata extends JobTriggerCompositeService.Metadata {

        static final TriggerByJobMetadata INSTANCE = new TriggerByJobMetadata();

        @Override
        public @NonNull String requestKeyName() { return "job_id"; }

        @Override
        public @NonNull String jsonKeyName() {
            return "job_id";
        }

        @Override
        public @NonNull String pluralKeyName() {
            return "triggers";
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class JobByTriggerMetadata extends JobTriggerCompositeService.Metadata {

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
        public @NonNull String pluralKeyName() {
            return "jobs";
        }

    }


    final class JobTriggerComposite extends JobTrigger implements CompositePojo<JobTrigger> {

        private final Map<String, VertxPojo> other = new HashMap<>();

        @Override
        public @NonNull Map<String, VertxPojo> other() {
            return other;
        }

        @Override
        public JsonObject toJson() {
            return super.toJson().mergeIn(otherToJson(), true);
        }

    }

}
