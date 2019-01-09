package com.nubeiot.edge.connector.bonescript;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoneScriptEventBus {

    public static final EventModel POINTS = EventModel.builder()
                                                      .address("nubeiot.edge.connector.bonescript.points")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .events(Arrays.asList(EventAction.GET_LIST, EventAction.CREATE,
                                                                            EventAction.PATCH, EventAction.REMOVE))
                                                      .build();

}
