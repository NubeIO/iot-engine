package com.nubeiot.core.http.client.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventModel;

import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WebsocketClientWriter implements EventHandler {

    private final WebSocket webSocket;
    private final EventModel publisher;

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(publisher.getEvents()));
    }

    @EventContractor(action = EventAction.SEND, returnType = int.class)
    public int send(JsonObject data) {
        webSocket.writeTextMessage(data.encode());
        return 1;
    }

}
