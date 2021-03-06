package com.nubeiot.scheduler.solution;

public interface Job {

    void execute();

    interface JobBuilder<J extends Job> {

        J build();

    }

}
