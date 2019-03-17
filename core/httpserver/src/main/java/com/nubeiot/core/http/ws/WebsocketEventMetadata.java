package com.nubeiot.core.http.ws;

import java.util.Objects;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.base.Urls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines {@code sub path} of Web Socket with server listener event and server publisher event.
 *
 * @see EventModel
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public final class WebsocketEventMetadata {

    /**
     * Websocket path
     */
    @EqualsAndHashCode.Include
    private final String path;
    /**
     * Event listener from client
     */
    private final EventModel listener;
    /**
     * Event processor for handling data from client
     */
    private final EventModel processor;
    /**
     * Event publisher to client
     */
    private final EventModel publisher;

    public static WebsocketEventMetadata create(EventModel listener, EventModel processor) {
        return create(null, listener, processor, null);
    }

    public static WebsocketEventMetadata create(String path, EventModel listener, EventModel processor) {
        return create(path, listener, processor, null);
    }

    public static WebsocketEventMetadata create(EventModel listener, EventModel processor, EventModel publisher) {
        return create(null, listener, processor, publisher);
    }

    public static WebsocketEventMetadata create(String path, EventModel listener, EventModel processor,
                                                EventModel publisher) {
        if (Objects.isNull(listener) || Objects.isNull(processor)) {
            throw new InitializerError("Must provide both listener and processor");
        }
        if (listener.getPattern() != EventPattern.REQUEST_RESPONSE) {
            throw new InitializerError("Unsupported listener event pattern " + listener.getPattern());
        }
        return new WebsocketEventMetadata(Urls.combinePath(path), listener, processor, publisher);
    }

    public static WebsocketEventMetadata create(EventModel publisher) {
        return create((String) null, publisher);
    }

    public static WebsocketEventMetadata create(String path, EventModel publisher) {
        if (Objects.isNull(publisher)) {
            throw new InitializerError("Must provide publisher");
        }
        return new WebsocketEventMetadata(Urls.combinePath(path), null, null, publisher);
    }

}
