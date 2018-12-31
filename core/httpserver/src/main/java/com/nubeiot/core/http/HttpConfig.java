package com.nubeiot.core.http;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class HttpConfig implements IConfig {

    public static final String NAME = "__http__";
    private String host = "0.0.0.0";
    private int port = 8080;
    private boolean enabled = true;
    private String rootApi = ApiConstants.ROOT_API_PATH;
    @JsonProperty(value = ServerOptions.NAME)
    private ServerOptions options = (ServerOptions) new ServerOptions().setCompressionSupported(true)
                                                                       .setDecompressionSupported(true);
    @JsonProperty(value = WebsocketConfig.NAME)
    private WebsocketConfig websocketCfg = new WebsocketConfig();
    @JsonProperty(value = Http2Config.NAME)
    private Http2Config http2Cfg = new Http2Config();
    @JsonProperty(value = CorsOptions.NAME)
    private CorsOptions corsOptions = new CorsOptions();

    @Override
    public String name() { return NAME; }

    @Override
    public Class<? extends IConfig> parent() { return NubeConfig.AppConfig.class; }

    @NoArgsConstructor
    public static class ServerOptions extends HttpServerOptions implements IConfig {

        public static final String NAME = "__server__";

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

        @Override
        public JsonObject toJson() { return super.toJson(); }

    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebsocketConfig implements IConfig {

        public static final String NAME = "__socket__";
        private boolean enabled = false;
        private int port = 8080;
        private String rootWs = ApiConstants.ROOT_WS_PATH;
        @JsonProperty(value = SockJSConfig.NAME)
        private SockJSConfig sockjsOptions = new SockJSConfig();
        @JsonProperty(value = SocketBridgeConfig.NAME)
        private SocketBridgeConfig bridgeOptions = new SocketBridgeConfig();

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

        public static class SockJSConfig extends SockJSHandlerOptions implements IConfig {

            public static final String NAME = "__sockjs__";

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return WebsocketConfig.class; }

        }


        public static class SocketBridgeConfig extends BridgeOptions implements IConfig {

            public static final String NAME = "__bridge__";

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return WebsocketConfig.class; }

        }

    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Http2Config implements IConfig {

        public static final String NAME = "__http2__";

        private boolean enabled = false;
        private int port = 8080;

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorsOptions implements IConfig {

        public static final String NAME = "__cors__";

        private String allowedOriginPattern = "*";
        private Set<HttpMethod> allowedMethods = ApiConstants.DEFAULT_CORS_HTTP_METHOD;
        private Set<String> allowedHeaders = new HashSet<>();
        private Set<String> exposedHeaders = new HashSet<>();
        private boolean allowCredentials = false;
        private int maxAgeSeconds = 3600;

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

    }

}
