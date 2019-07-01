package com.nubeiot.scheduler;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
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
    @JsonSubTypes.Type(value = EventJobModel.class, name = "EVENT_JOB")
})
public interface JobModel<T extends VertxJob> extends JsonData {

    String JOB_DATA_KEY = "jobModel";

    static JobKey createKey(String group, String name) {
        return new JobKey(Strings.isBlank(name) ? Key.createUniqueName(group) : name, group);
    }

    @JsonUnwrapped
    JobKey getKey();

    @JsonProperty(value = "type", required = true)
    JobType type();

    Class<T> implementation();

    default JobDetail toJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JOB_DATA_KEY, this);
        return JobBuilder.newJob(implementation()).withIdentity(getKey()).setJobData(jobDataMap).build();
    }

    enum JobType {
        EVENT_JOB
    }

}
