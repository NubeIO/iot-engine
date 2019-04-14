package com.nubeiot.core.http.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.HttpConfig.WebsocketConfig;
import com.nubeiot.core.http.base.InvalidUrlException;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;
import com.nubeiot.core.http.handler.WebsocketBridgeEventHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WebsocketEventBuilder {

    private final Logger logger = LoggerFactory.getLogger(WebsocketEventBuilder.class);
    private final Vertx vertx;
    private final Router router;
    private final Map<String, List<WebsocketServerEventMetadata>> socketsByPath = new HashMap<>();
    private WebsocketConfig websocketConfig;
    private Class<? extends WebsocketBridgeEventHandler> bridgeHandlerClass = WebsocketBridgeEventHandler.class;
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
                throw new InvalidUrlException("Root Websocket is not valid");
            }
            this.rootWs = root;
        }
        return this;
    }

    public WebsocketEventBuilder register(@NonNull WebsocketServerEventMetadata socketMetadata) {
        socketsByPath.computeIfAbsent(socketMetadata.getPath(), k -> new ArrayList<>()).add(socketMetadata);
        return this;
    }

    public WebsocketEventBuilder register(@NonNull WebsocketServerEventMetadata... eventBusSockets) {
        return this.register(Arrays.asList(eventBusSockets));
    }

    public WebsocketEventBuilder register(@NonNull Collection<WebsocketServerEventMetadata> eventBusSockets) {
        eventBusSockets.stream().filter(Objects::nonNull).forEach(this::register);
        return this;
    }

    public WebsocketEventBuilder handler(@NonNull Class<? extends WebsocketBridgeEventHandler> handler) {
        this.bridgeHandlerClass = handler;
        return this;
    }

    public WebsocketEventBuilder options(@NonNull WebsocketConfig websocketConfig) {
        this.websocketConfig = websocketConfig;
        return this;
    }

    public Router build() {
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, config().getSockjsOptions());
        EventController controller = new EventController(vertx);
        validate().forEach((path, socketMapping) -> {
            String fullPath = Urls.combinePath(rootWs, path, ApiConstants.WILDCARDS_ANY_PATH);
            router.route(fullPath)
                  .handler(sockJSHandler.bridge(createBridgeOptions(fullPath, socketMapping),
                                                createHandler(controller, socketMapping)));
        });
        return router;
    }

    Map<String, List<WebsocketServerEventMetadata>> validate() {
        if (this.socketsByPath.isEmpty()) {
            throw new InitializerError("No socket handler given, register at least one.");
        }
        return socketsByPath;
    }

    private BridgeOptions createBridgeOptions(String fullPath, List<WebsocketServerEventMetadata> metadata) {
        BridgeOptions opts = new BridgeOptions(config().getBridgeOptions());
        metadata.forEach(m -> {
            EventModel listener = m.getListener();
            EventModel publisher = m.getPublisher();
            if (Objects.nonNull(listener)) {
                logger.info("Registering websocket | Event Listener :\t{} --- {} {}", fullPath, listener.getPattern(),
                            listener.getAddress());
                opts.addInboundPermitted(new PermittedOptions().setAddress(listener.getAddress()));
            } else if (Objects.nonNull(publisher)) {
                logger.info("Registering websocket | Event Publisher:\t{} --- {} {}", fullPath, publisher.getPattern(),
                            publisher.getAddress());
                opts.addOutboundPermitted(new PermittedOptions().setAddress(publisher.getAddress()));
            }
        });
        return opts;
    }

    private WebsocketBridgeEventHandler createHandler(EventController controller,
                                                      List<WebsocketServerEventMetadata> socketMapping) {
        Map<Class, Object> map = new LinkedHashMap<>();
        map.put(EventController.class, controller);
        map.put(List.class, socketMapping);
        WebsocketBridgeEventHandler handler = ReflectionClass.createObject(bridgeHandlerClass, map);
        return Objects.isNull(handler) ? new WebsocketBridgeEventHandler(controller, socketMapping) : handler;
    }

    private WebsocketConfig config() {
        return Objects.requireNonNull(websocketConfig);
    }

}
