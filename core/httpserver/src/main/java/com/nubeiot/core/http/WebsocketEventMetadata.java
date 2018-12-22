package com.nubeiot.core.http;

import java.util.Objects;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.utils.Urls;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Defines {@code sub path} of Web Socket with server listener event and server publisher event.
 *
 * @see EventModel
 */
@Getter
@Builder(builderClassName = "Builder")
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

    private WebsocketEventMetadata(String path, EventModel listener, EventModel processor, EventModel publisher) {
        try {
            this.path = Urls.combinePath(path);
            this.listener = Objects.requireNonNull(listener, "Must provide listener");
            this.publisher = Objects.isNull(publisher) ? listener : publisher;
            this.processor = Objects.requireNonNull(processor, "Must provide processor");
        } catch (NullPointerException e) {
            throw new InitializerError("Error when initialize metadata", e);
        }
    }

}
