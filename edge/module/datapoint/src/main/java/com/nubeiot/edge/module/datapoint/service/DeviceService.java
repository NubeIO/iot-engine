package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.DeviceDao;
import com.nubeiot.iotdata.model.tables.pojos.Device;
import com.nubeiot.iotdata.model.tables.records.DeviceRecord;

import lombok.NonNull;

public final class DeviceService extends AbstractDataPointService<UUID, Device, DeviceRecord, DeviceDao>
    implements UUIDKeyEntity<Device, DeviceRecord, DeviceDao> {

    static String REQUEST_KEY = EntityService.createRequestKeyName(Device.class, Tables.DEVICE.ID.getName());

    public DeviceService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    @Override
    @NonNull
    public String listKey() {
        return "devices";
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
