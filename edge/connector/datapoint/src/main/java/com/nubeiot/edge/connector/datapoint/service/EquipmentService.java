package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.EquipmentDao;
import com.nubeiot.iotdata.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.model.tables.records.EquipmentRecord;

import lombok.NonNull;

public final class EquipmentService extends AbstractDittoService<UUID, Equipment, EquipmentRecord, EquipmentDao>
    implements UUIDKeyEntity<Equipment, EquipmentRecord, EquipmentDao> {

    public EquipmentService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull String listKey() {
        return "equipments";
    }

    @Override
    public @NonNull Class<Equipment> modelClass() {
        return Equipment.class;
    }

    @Override
    public @NonNull Class<EquipmentDao> daoClass() {
        return EquipmentDao.class;
    }

    @Override
    public @NonNull JsonTable<EquipmentRecord> table() {
        return Tables.EQUIPMENT;
    }

    @Override
    public Equipment parse(@NonNull JsonObject request) throws IllegalArgumentException {
        return new Equipment(request);
    }

    @Override
    public String endpoint() {
        return null;
    }

}
