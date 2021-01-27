package com.nubeiot.core.rpc.watcher;

import io.github.zero88.qwe.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class WatcherOption implements JsonData {

    /**
     * Enable realtime mode that run when any event is occurred in a watcher object
     */
    private final boolean realtime;
    /**
     * Enable trigger mode that run on a schedule or periodical, such as reading a sensor every five milliseconds
     */
    private final boolean trigger;

    /**
     * Defines trigger option if enable trigger mode
     */
    private final TriggerOption triggerOption;

}
