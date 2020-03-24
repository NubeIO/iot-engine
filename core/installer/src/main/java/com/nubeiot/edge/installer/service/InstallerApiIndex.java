package com.nubeiot.edge.installer.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jooq.OrderField;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.EntityMetadata.StringKeyEntity;
import com.nubeiot.core.sql.EntityMetadata.UUIDKeyEntity;
import com.nubeiot.core.sql.MetadataIndex;
import com.nubeiot.edge.installer.model.Tables;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationBackupDao;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationDao;
import com.nubeiot.edge.installer.model.tables.daos.ApplicationHistoryDao;
import com.nubeiot.edge.installer.model.tables.daos.DeployTransactionDao;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.model.tables.pojos.ApplicationBackup;
import com.nubeiot.edge.installer.model.tables.pojos.ApplicationHistory;
import com.nubeiot.edge.installer.model.tables.pojos.DeployTransaction;
import com.nubeiot.edge.installer.model.tables.records.ApplicationBackupRecord;
import com.nubeiot.edge.installer.model.tables.records.ApplicationHistoryRecord;
import com.nubeiot.edge.installer.model.tables.records.ApplicationRecord;
import com.nubeiot.edge.installer.model.tables.records.DeployTransactionRecord;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public interface InstallerApiIndex extends MetadataIndex {

    List<EntityMetadata> INDEX = Collections.unmodifiableList(MetadataIndex.find(InstallerApiIndex.class));

    @Override
    default List<EntityMetadata> index() {
        return INDEX;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class ApplicationMetadata implements StringKeyEntity<Application, ApplicationRecord, ApplicationDao> {

        public static final ApplicationMetadata INSTANCE = new ApplicationMetadata();

        @Override
        public @NonNull com.nubeiot.edge.installer.model.tables.Application table() {
            return Tables.APPLICATION;
        }

        @Override
        public @NonNull Class<Application> modelClass() {
            return Application.class;
        }

        @Override
        public @NonNull Class<ApplicationDao> daoClass() {
            return ApplicationDao.class;
        }

        @Override
        public @NonNull String requestKeyName() { return "app_id"; }

        @Override
        public @NonNull String singularKeyName() { return "app"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().STATE, table().SERVICE_TYPE, table().APP_ID);
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TransactionMetadata
        implements StringKeyEntity<DeployTransaction, DeployTransactionRecord, DeployTransactionDao> {

        public static final TransactionMetadata INSTANCE = new TransactionMetadata();

        @Override
        public @NonNull com.nubeiot.edge.installer.model.tables.DeployTransaction table() {
            return Tables.DEPLOY_TRANSACTION;
        }

        @Override
        public @NonNull Class<DeployTransaction> modelClass() {
            return DeployTransaction.class;
        }

        @Override
        public @NonNull Class<DeployTransactionDao> daoClass() {
            return DeployTransactionDao.class;
        }

        @Override
        public @NonNull String requestKeyName() { return "transaction_id"; }

        @Override
        public @NonNull String singularKeyName() { return "transaction"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().APP_ID, table().MODIFIED_AT.desc());
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistoryMetadata
        implements StringKeyEntity<ApplicationHistory, ApplicationHistoryRecord, ApplicationHistoryDao> {

        public static final HistoryMetadata INSTANCE = new HistoryMetadata();

        @Override
        public @NonNull com.nubeiot.edge.installer.model.tables.ApplicationHistory table() {
            return Tables.APPLICATION_HISTORY;
        }

        @Override
        public @NonNull Class<ApplicationHistory> modelClass() {
            return ApplicationHistory.class;
        }

        @Override
        public @NonNull Class<ApplicationHistoryDao> daoClass() {
            return ApplicationHistoryDao.class;
        }

        @Override
        public @NonNull String requestKeyName() { return "transaction_id"; }

        @Override
        public @NonNull String singularKeyName() { return "transaction"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().APP_ID, table().MODIFIED_AT.desc());
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class BackupMetadata
        implements UUIDKeyEntity<ApplicationBackup, ApplicationBackupRecord, ApplicationBackupDao> {

        public static final BackupMetadata INSTANCE = new BackupMetadata();

        @Override
        public @NonNull com.nubeiot.edge.installer.model.tables.ApplicationBackup table() {
            return Tables.APPLICATION_BACKUP;
        }

        @Override
        public @NonNull Class<ApplicationBackup> modelClass() {
            return ApplicationBackup.class;
        }

        @Override
        public @NonNull Class<ApplicationBackupDao> daoClass() {
            return ApplicationBackupDao.class;
        }

        @Override
        public @NonNull String requestKeyName() { return "backup_id"; }

        @Override
        public @NonNull String singularKeyName() { return "backup"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Collections.singletonList(table().APP_ID);
        }

    }

}
