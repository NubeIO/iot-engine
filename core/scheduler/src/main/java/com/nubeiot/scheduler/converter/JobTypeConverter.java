package com.nubeiot.scheduler.converter;

import com.nubeiot.core.sql.converter.AbstractEnumConverter;
import com.nubeiot.scheduler.job.JobType;

public class JobTypeConverter extends AbstractEnumConverter<JobType> {

    @Override
    public Class<JobType> toType() {
        return JobType.class;
    }

    @Override
    protected JobType def() {
        return null;
    }

}
