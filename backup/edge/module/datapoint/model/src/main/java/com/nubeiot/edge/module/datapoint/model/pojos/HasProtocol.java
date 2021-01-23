package com.nubeiot.edge.module.datapoint.model.pojos;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

public interface HasProtocol<T extends VertxPojo> {

    @NonNull Protocol getProtocol(@NonNull T pojo);

}
