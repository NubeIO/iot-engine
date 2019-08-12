package com.nubeiot.scheduler.job;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.dto.EnumType.AbstractEnumType;

public final class JobType extends AbstractEnumType {

    public static final JobType EVENT_JOB = new JobType("EVENT_JOB");

    private JobType(String type) {
        super(type);
    }

    @JsonCreator
    public static JobType factory(String type) {
        return EnumType.factory(type, JobType.class);
    }

}
