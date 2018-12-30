package com.nubeiot.core.http.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.component.IComponent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.reactivex.core.eventbus.EventBus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class MockEventBusHandler implements IComponent, EventHandler {

    private final EventBus eventBus;
    private final String address;
    @Getter
    private List<EventAction> availableEvents = Arrays.asList(EventAction.GET_LIST, EventAction.GET_ONE,
                                                              EventAction.CREATE, EventAction.UPDATE,
                                                              EventAction.PATCH);

    public MockEventBusHandler(EventBus eventBus, EventModel model) {
        this(eventBus, model.getAddress());
        this.availableEvents = new ArrayList<>(model.getEvents());
    }

    @Override
    public void start() throws NubeException {
        this.eventBus.consumer(address, this::accept);
    }

    @Override
    public void stop() throws NubeException {

    }

}
