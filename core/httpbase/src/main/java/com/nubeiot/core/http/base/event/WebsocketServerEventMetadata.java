package com.nubeiot.core.http.base.event;

import java.util.Objects;

import io.github.zero88.utils.Urls;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.InitializerError;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Defines {@code sub path} of Web Socket with server listener event and server publisher event.
 *
 * @see EventModel
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class WebsocketServerEventMetadata extends WebsocketClientEventMetadata {

    /**
     * Event processor for handling data from client
     */
    private final EventModel processor;

    private WebsocketServerEventMetadata(String path, EventModel listener, EventModel processor, EventModel publisher) {
        super(path, listener, publisher);
        this.processor = processor;
    }

    public static WebsocketServerEventMetadata create(EventModel listener, EventModel processor) {
        return create(null, listener, processor, null);
    }

    public static WebsocketServerEventMetadata create(String path, EventModel listener, EventModel processor) {
        return create(path, listener, processor, null);
    }

    public static WebsocketServerEventMetadata create(EventModel listener, EventModel processor, EventModel publisher) {
        return create(null, listener, processor, publisher);
    }

    public static WebsocketServerEventMetadata create(String path, EventModel listener, EventModel processor,
                                                      EventModel publisher) {
        if (Objects.isNull(listener) || Objects.isNull(processor)) {
            throw new InitializerError("Must provide both listener and processor");
        }
        if (listener.getPattern() != EventPattern.REQUEST_RESPONSE) {
            throw new InitializerError("Unsupported listener event pattern " + listener.getPattern());
        }
        return new WebsocketServerEventMetadata(Urls.combinePath(path), listener, processor, publisher);
    }

    public static WebsocketServerEventMetadata create(EventModel publisher) {
        return create((String) null, publisher);
    }

    public static WebsocketServerEventMetadata create(String path, EventModel publisher) {
        if (Objects.isNull(publisher)) {
            throw new InitializerError("Must provide publisher");
        }
        return new WebsocketServerEventMetadata(Urls.combinePath(path), null, null, publisher);
    }

}
