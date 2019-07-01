package com.nubeiot.scheduler;

import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.utils.Key;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.scheduler.job.EventJobModel;

@JsonTypeInfo(use = Id.NAME, property = "type", visible = true)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = EventJobModel.class, name = "CRON")
})
public interface TriggerModel extends JsonData {

    static TriggerKey createKey(String group, String name) {
        return new TriggerKey(Strings.isBlank(name) ? Key.createUniqueName(group) : name, group);
    }

    @JsonUnwrapped
    TriggerKey getKey();

    @JsonProperty(value = "type", required = true)
    TriggerType type();

    default Trigger toTrigger() {
        return null;
    }

    enum TriggerType {
        CRON, PERIODIC
    }

}
