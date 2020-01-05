package com.nubeiot.edge.module.scheduler.pojos;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.pojos.CompositePojo;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class JobTriggerComposite extends JobTrigger implements CompositePojo<JobTrigger, JobTriggerComposite> {

    @Getter
    private final ExtensionPojo extension = new ExtensionPojo();

    @Override
    public JobTriggerComposite wrap(JobTrigger pojo) {
        this.from(pojo);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return super.toJson().mergeIn(extensionToJson(), true);
    }

}
