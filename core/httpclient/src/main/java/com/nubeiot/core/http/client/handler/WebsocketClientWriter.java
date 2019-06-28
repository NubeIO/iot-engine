package com.nubeiot.core.http.client.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WebsocketClientWriter implements EventListener {

    private final WebSocket webSocket;
    private final EventModel publisher;

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(publisher.getEvents()));
    }

    @EventContractor(action = EventAction.SEND, returnType = boolean.class)
    public boolean send(JsonObject data) {
        webSocket.writeTextMessage(data.encode());
        return true;
    }

}
