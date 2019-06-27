package com.nubeiot.scheduler.solution.impl;

import java.util.List;

import com.nubeiot.scheduler.solution.JobDetail;
import com.nubeiot.scheduler.solution.JobKey;
import com.nubeiot.scheduler.solution.StoreScheduler;
import com.nubeiot.scheduler.solution.Trigger;
import com.nubeiot.scheduler.solution.TriggerKey;

public class RamStoreScheduler implements StoreScheduler {

    @Override
    public JobKey add(Trigger trigger, JobDetail definition) {
        return null;
    }

    @Override
    public TriggerKey remove(Trigger trigger) {
        return null;
    }

    @Override
    public JobKey remove(JobDetail trigger) {
        return null;
    }

    @Override
    public boolean inactive(TriggerKey trigger) {
        return false;
    }

    @Override
    public boolean inactive(JobKey trigger) {
        return false;
    }

    @Override
    public boolean inactive(TriggerKey trigger, JobKey jobKey) {
        return false;
    }

    @Override
    public List<JobDetail> jobs() {
        return null;
    }

    @Override
    public List<JobDetail> jobsByTrigger(TriggerKey trigger) {
        return null;
    }

    @Override
    public List<Trigger> triggers() {
        return null;
    }

    @Override
    public List<JobDetail> getInactiveJobs() {
        return null;
    }

    @Override
    public List<Trigger> getInactiveTriggers() {
        return null;
    }

}
