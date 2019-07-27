package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.DeviceDao;
import com.nubeiot.iotdata.model.tables.pojos.Device;
import com.nubeiot.iotdata.model.tables.records.DeviceRecord;

import lombok.NonNull;

public final class DeviceService extends AbstractDittoService<UUID, Device, DeviceRecord, DeviceDao>
    implements UUIDKeyEntity<Device, DeviceRecord, DeviceDao> {

    public DeviceService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    protected @NonNull String listKey() {
        return "devices";
    }

    @Override
    public String endpoint() {
        return "/device";
    }

    @Override
    public @NonNull Class<Device> modelClass() {
        return Device.class;
    }

    @Override
    public @NonNull Class<DeviceDao> daoClass() {
        return DeviceDao.class;
    }

    @Override
    public @NonNull JsonTable<DeviceRecord> table() {
        return Tables.DEVICE;
    }

}
