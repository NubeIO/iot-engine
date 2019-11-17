package com.nubeiot.core.sql.service;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;

import lombok.NonNull;

public interface EntityLifecycleTask {

    Single<VertxPojo> validateInput(@NonNull RequestData reqData);

    Single<VertxPojo> remoteValidate(@NonNull VertxPojo pojo);

    Single<VertxPojo> execute(VertxPojo pojo);

    Single<VertxPojo> asyncExecuteAfterPersist(VertxPojo pojo);

    Single<JsonObject> response(VertxPojo pojo);

}
