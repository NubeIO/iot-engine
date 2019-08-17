package com.nubeiot.edge.module.scheduler.service;

import java.util.HashMap;
import java.util.Map;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;

import lombok.NonNull;

public final class JobTriggerComposite extends JobTrigger implements CompositePojo<JobTrigger, JobTriggerComposite> {

    private final Map<String, VertxPojo> other = new HashMap<>();

    @Override
    public @NonNull Map<String, VertxPojo> other() {
        return other;
    }

    @Override
    public Class<JobTrigger> pojoClass() {
        return JobTrigger.class;
    }

    @Override
    public JsonObject toJson() {
        return super.toJson().mergeIn(otherToJson(), true);
    }

}
