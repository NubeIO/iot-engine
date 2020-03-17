package com.nubeiot.edge.module.datapoint.service;

import com.nubeiot.core.sql.service.EntityApiService;

import lombok.NonNull;

public interface DataPointApiService extends EntityApiService {

    DataPointApiService DEFAULT = new DataPointApiService() {};

    @Override
    default @NonNull String prefixServiceName() {
        return "bios.datapoint";
    }

}
