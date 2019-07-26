package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.core.sql.ModelService.UUIDKeyModel;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.EquipmentDao;
import com.nubeiot.iotdata.model.tables.pojos.Equipment;
import com.nubeiot.iotdata.model.tables.records.EquipmentRecord;

import lombok.NonNull;

public final class EquipmentService extends AbstractDittoService<UUID, Equipment, EquipmentRecord, EquipmentDao>
    implements UUIDKeyModel<Equipment, EquipmentRecord, EquipmentDao> {

    public EquipmentService(EquipmentDao dao) {
        super(dao);
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
    protected @NonNull String listKey() {
        return "equipments";
    }

    @Override
    public String endpoint() {
        return null;
    }

}
