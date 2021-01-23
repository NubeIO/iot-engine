package com.nubeiot.scheduler.solution;

import com.nubeiot.scheduler.solution.Job.JobBuilder;

public interface JobDetail<J extends Job> {

    JobKey key();

    JobBuilder<J> builder();

    default int priority() {
        return 1000;
    }

}
