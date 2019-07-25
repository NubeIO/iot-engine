package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import org.jooq.Table;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.AbstractModelService;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.DeviceDao;
import com.nubeiot.iotdata.model.tables.pojos.Device;
import com.nubeiot.iotdata.model.tables.records.DeviceRecord;

import lombok.NonNull;

public final class DeviceService extends AbstractModelService<UUID, Device, DeviceRecord, DeviceDao>
    implements DittoService {

    DeviceService(DeviceDao dao) {
        super(dao);
    }

    @Override
    public String endpoint() {
        return "/device";
    }

    @Override
    protected Device parse(JsonObject object) {
        return null;
    }

    @Override
    protected @NonNull Table<DeviceRecord> table() {
        return Tables.DEVICE;
    }

    @Override
    protected UUID id(String requestKey) throws IllegalArgumentException {
        return UUID.fromString(requestKey);
    }

    @Override
    public boolean hasTimeAudit() { return true; }

    @Override
    protected @NonNull String listKey() {
        return "devices";
    }

    @Override
    protected Device validateOnCreate(Device pojo) throws IllegalArgumentException {
        return pojo;
    }

    @Override
    protected Device validateOnUpdate(Device pojo) throws IllegalArgumentException {
        return pojo;
    }

    @Override
    protected Device validateOnPatch(Device pojo) throws IllegalArgumentException {
        return pojo;
    }

}
