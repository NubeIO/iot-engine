package com.nubeiot.core.http.base.event;

import io.github.zero88.utils.Strings;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for {@code Websocket Client} event definition
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public class WebsocketClientEventMetadata {

    /**
     * Websocket path. {@code Nullable} if using root {@code websocket} path
     */
    @EqualsAndHashCode.Include
    private final String path;
    /**
     * When {@code websocket message} comes, it will dispatched to {@code listener} address
     */
    private final EventModel listener;
    /**
     * Push message to {@code publisher} address, it will send {@code websocket message} to subscriber
     */
    private final EventModel publisher;

    public static WebsocketClientEventMetadata create(@NonNull EventModel listener, String publisherAddress) {
        return create("", listener, publisherAddress);
    }

    public static WebsocketClientEventMetadata create(String path, @NonNull EventModel listener,
                                                      String publisherAddress) {
        return create(path, listener, EventModel.builder()
                                                .address(Strings.requireNotBlank(publisherAddress))
                                                .pattern(EventPattern.POINT_2_POINT)
                                                .local(true)
                                                .event(EventAction.SEND)
                                                .build());
    }

    public static WebsocketClientEventMetadata create(String path, @NonNull EventModel listener,
                                                      @NonNull EventModel publisher) {
        return new WebsocketClientEventMetadata(path, listener, publisher);
    }

}
