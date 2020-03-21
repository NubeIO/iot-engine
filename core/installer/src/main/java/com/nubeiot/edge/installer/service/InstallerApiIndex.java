package com.nubeiot.edge.installer.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jooq.OrderField;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.EntityMetadata.StringKeyEntity;
import com.nubeiot.core.sql.MetadataIndex;
import com.nubeiot.edge.installer.model.Tables;
import com.nubeiot.edge.installer.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.installer.model.tables.daos.TblRemoveHistoryDao;
import com.nubeiot.edge.installer.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.installer.model.tables.pojos.TblModule;
import com.nubeiot.edge.installer.model.tables.pojos.TblRemoveHistory;
import com.nubeiot.edge.installer.model.tables.pojos.TblTransaction;
import com.nubeiot.edge.installer.model.tables.records.TblModuleRecord;
import com.nubeiot.edge.installer.model.tables.records.TblRemoveHistoryRecord;
import com.nubeiot.edge.installer.model.tables.records.TblTransactionRecord;

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
    final class ApplicationMetadata implements StringKeyEntity<TblModule, TblModuleRecord, TblModuleDao> {

        public static final ApplicationMetadata INSTANCE = new ApplicationMetadata();

        @Override
        public @NonNull com.nubeiot.edge.installer.model.tables.TblModule table() {
            return Tables.TBL_MODULE;
        }

        @Override
        public @NonNull Class<TblModule> modelClass() {
            return TblModule.class;
        }

        @Override
        public @NonNull Class<TblModuleDao> daoClass() {
            return TblModuleDao.class;
        }

        @Override
        public @NonNull String requestKeyName() { return "service_id"; }

        @Override
        public @NonNull String singularKeyName() { return "app"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().STATE, table().SERVICE_TYPE, table().SERVICE_ID);
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class TransactionMetadata
        implements StringKeyEntity<TblTransaction, TblTransactionRecord, TblTransactionDao> {

        public static final TransactionMetadata INSTANCE = new TransactionMetadata();

        @Override
        public @NonNull com.nubeiot.edge.installer.model.tables.TblTransaction table() {
            return Tables.TBL_TRANSACTION;
        }

        @Override
        public @NonNull Class<TblTransaction> modelClass() {
            return TblTransaction.class;
        }

        @Override
        public @NonNull Class<TblTransactionDao> daoClass() {
            return TblTransactionDao.class;
        }

        @Override
        public @NonNull String requestKeyName() { return "transaction_id"; }

        @Override
        public @NonNull String singularKeyName() { return "transaction"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().MODULE_ID, table().MODIFIED_AT.desc());
        }

    }


    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class HistoryMetadata
        implements StringKeyEntity<TblRemoveHistory, TblRemoveHistoryRecord, TblRemoveHistoryDao> {

        public static final HistoryMetadata INSTANCE = new HistoryMetadata();

        @Override
        public @NonNull com.nubeiot.edge.installer.model.tables.TblRemoveHistory table() {
            return Tables.TBL_REMOVE_HISTORY;
        }

        @Override
        public @NonNull Class<TblRemoveHistory> modelClass() {
            return TblRemoveHistory.class;
        }

        @Override
        public @NonNull Class<TblRemoveHistoryDao> daoClass() {
            return TblRemoveHistoryDao.class;
        }

        @Override
        public @NonNull String requestKeyName() { return "transaction_id"; }

        @Override
        public @NonNull String singularKeyName() { return "transaction"; }

        @Override
        public @NonNull List<OrderField<?>> orderFields() {
            return Arrays.asList(table().MODULE_ID, table().MODIFIED_AT.desc());
        }

    }

}
