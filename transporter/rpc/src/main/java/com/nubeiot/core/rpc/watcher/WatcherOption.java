package com.nubeiot.core.rpc.watcher;

import io.github.zero88.qwe.dto.JsonData;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WatcherOption implements JsonData {

    /**
     * Enable realtime task that run when any event is occurred in a watcher object
     */
    private final boolean realtime;
    /**
     * Enable periodic task that run on a fixed schedule, such as reading a sensor every five milliseconds
     */
    private final boolean periodic;

}
