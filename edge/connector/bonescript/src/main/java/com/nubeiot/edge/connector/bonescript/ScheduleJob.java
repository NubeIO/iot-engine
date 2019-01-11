package com.nubeiot.edge.connector.bonescript;

import org.quartz.Scheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class ScheduleJob {

    private final String schedule;
    @Setter
    private Scheduler scheduler;

}
