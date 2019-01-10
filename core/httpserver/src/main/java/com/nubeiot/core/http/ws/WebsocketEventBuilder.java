package com.nubeiot.core.http.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.InvalidUrlException;
import com.nubeiot.core.http.handler.WebsocketBridgeEventHandler;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
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
    private Class<? extends WebsocketBridgeEventHandler> bridgeHandlerClass = WebsocketBridgeEventHandler.class;
    @Getter(AccessLevel.PACKAGE)
    private String rootWs = ApiConstants.ROOT_WS_PATH;
    private HttpConfig.WebsocketConfig sockJsOption = new HttpConfig.WebsocketConfig();

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

    public WebsocketEventBuilder handler(@NonNull Class<? extends WebsocketBridgeEventHandler> handler) {
        this.bridgeHandlerClass = handler;
        return this;
    }

    public WebsocketEventBuilder options(@NonNull HttpConfig.WebsocketConfig sockJsOptions) {
        this.sockJsOption = sockJsOptions;
        return this;
    }

    public Router build() {
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJsOption.getSockjsOptions());
        EventController controller = new EventController(vertx);
        validate().forEach((path, socketMapping) -> {
            String fullPath = Urls.combinePath(rootWs, path, ApiConstants.PATH_WILDCARDS);
            router.route(fullPath)
                  .handler(sockJSHandler.bridge(createBridgeOptions(fullPath, socketMapping),
                                                createHandler(controller, socketMapping)));
        });
        return router;
    }

    Map<String, List<WebsocketEventMetadata>> validate() {
        if (this.socketsByPath.isEmpty()) {
            throw new InitializerError("No socket handler given, register at least one.");
        }
        return socketsByPath;
    }

    private BridgeOptions createBridgeOptions(String fullPath, List<WebsocketEventMetadata> metadata) {
        BridgeOptions opts = new BridgeOptions(sockJsOption.getBridgeOptions());
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
                                                      List<WebsocketEventMetadata> socketMapping) {
        Map<Class, Object> map = new LinkedHashMap<>();
        map.put(EventController.class, controller);
        map.put(List.class, socketMapping);
        WebsocketBridgeEventHandler handler = ReflectionClass.createObject(bridgeHandlerClass, map);
        return Objects.isNull(handler) ? new WebsocketBridgeEventHandler(controller, socketMapping) : handler;
    }

}
