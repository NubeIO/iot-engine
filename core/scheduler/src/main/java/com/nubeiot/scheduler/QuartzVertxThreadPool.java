package com.nubeiot.scheduler;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

public class QuartzVertxThreadPool implements ThreadPool {

    @Override
    public boolean runInThread(Runnable runnable) {
        return false;
    }

    @Override
    public int blockForAvailableThreads() {
        return 0;
    }

    @Override
    public void initialize() throws SchedulerConfigException {

    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {

    }

    @Override
    public int getPoolSize() {
        return 0;
    }

    @Override
    public void setInstanceId(String schedInstId) {

    }

    @Override
    public void setInstanceName(String schedName) {

    }

}
