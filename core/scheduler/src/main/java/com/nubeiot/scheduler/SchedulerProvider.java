package com.nubeiot.scheduler;

import com.nubeiot.core.component.UnitProvider;

public final class SchedulerProvider implements UnitProvider<QuartzSchedulerUnit> {

    @Override
    public Class<QuartzSchedulerUnit> unitClass() { return QuartzSchedulerUnit.class; }

    @Override
    public QuartzSchedulerUnit get() { return new QuartzSchedulerUnit(); }

}
