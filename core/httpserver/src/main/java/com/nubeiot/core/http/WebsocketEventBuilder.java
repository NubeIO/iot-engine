package com.nubeiot.core.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.handler.FailureContextHandler;
import com.nubeiot.core.http.handler.WebsocketEventHandler;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WebsocketEventBuilder {

    private final Logger logger = LoggerFactory.getLogger(WebsocketEventBuilder.class);
    private final Vertx vertx;
    private final Router router;
    private final Map<String, List<WebsocketEventMetadata>> socketsByPath = new HashMap<>();
    @Getter(AccessLevel.PACKAGE)
    private String rootWs = ApiConstants.ROOT_WS_PATH;

    /**
     * For test
     */
    WebsocketEventBuilder() {
        this(null, null);
    }

    public WebsocketEventBuilder rootWs(String rootWs) {
        if (Strings.isNotBlank(rootWs)) {
            String root = Urls.combinePath(rootWs);
            if (!Urls.validatePath(root)) {
                throw new InvalidUrlException("Root API is not valid");
            }
            this.rootWs = root;
        }
        return this;
    }

    public WebsocketEventBuilder register(@NonNull WebsocketEventMetadata socketMetadata) {
        socketsByPath.computeIfAbsent(socketMetadata.getPath(), k -> new ArrayList<>()).add(socketMetadata);
        return this;
    }

    public WebsocketEventBuilder register(@NonNull WebsocketEventMetadata... eventBusSockets) {
        return this.register(Arrays.asList(eventBusSockets));
    }

    public WebsocketEventBuilder register(@NonNull Collection<WebsocketEventMetadata> eventBusSockets) {
        eventBusSockets.stream().filter(Objects::nonNull).forEach(this::register);
        return this;
    }

    public Router build() {
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        EventController controller = new EventController(vertx);
        validate().forEach((path, socketMapping) -> {
            String fullPath = Urls.combinePath(rootWs, path, ApiConstants.PATH_WILDCARDS);
            router.route(fullPath)
                  .handler(sockJSHandler.bridge(create(fullPath, socketMapping),
                                                new WebsocketEventHandler(controller, socketMapping)))
                  .failureHandler(new FailureContextHandler());
        });
        return router;
    }

    Map<String, List<WebsocketEventMetadata>> validate() {
        if (this.socketsByPath.isEmpty()) {
            throw new InitializerError("No socket handler given, register at least one.");
        }
        return socketsByPath;
    }

    private BridgeOptions create(String fullPath, List<WebsocketEventMetadata> metadata) {
        BridgeOptions opts = new BridgeOptions();
        metadata.forEach(m -> {
            EventModel listener = m.getListener();
            EventModel publisher = m.getPublisher();
            logger.info("Registering websocket | Event Listener :\t{} --- {} {}", fullPath, listener.getPattern(),
                        listener.getAddress());
            logger.info("Registering websocket | Event Publisher:\t{} --- {} {}", fullPath, publisher.getPattern(),
                        publisher.getAddress());
            opts.addInboundPermitted(new PermittedOptions().setAddress(listener.getAddress()));
            opts.addOutboundPermitted(new PermittedOptions().setAddress(publisher.getAddress()));
        });
        return opts;
    }

}
