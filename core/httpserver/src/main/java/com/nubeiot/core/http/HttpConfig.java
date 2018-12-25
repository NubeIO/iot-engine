package com.nubeiot.core.http;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpConfig {

    private String host = "0.0.0.0";
    private int port = 8080;
    private boolean enabled = true;
    private String rootApi = ApiConstants.ROOT_API_PATH;
    private HttpServerOptions options = new HttpServerOptions().setCompressionSupported(true);
    @JsonProperty(value = "__socket__")
    private WebsocketConfig websocketCfg = new WebsocketConfig();
    @JsonProperty(value = "__http2__")
    private Http2Config http2Cfg = new Http2Config();
    @JsonProperty(value = "__cors__")
    private CorsOptions corsOptions = new CorsOptions();

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    @Getter
    public static class WebsocketConfig {

        private boolean enabled = false;
        private int port = 8080;
        private String rootWs = ApiConstants.ROOT_WS_PATH;
        private SockJSHandlerOptions options = new SockJSHandlerOptions();
        private BridgeOptions bridgeOptions = new BridgeOptions();

    }


    @Getter
    public static class Http2Config {

        private boolean enabled = false;
        private int port = 8080;

    }


    @Getter
    public static class CorsOptions {

        private String allowedOriginPattern = "*";
        private Set<HttpMethod> allowedMethods = ApiConstants.DEFAULT_CORS_HTTP_METHOD;
        private Set<String> allowedHeaders = new HashSet<>();
        private Set<String> exposedHeaders = new HashSet<>();
        private boolean allowCredentials = false;
        private int maxAgeSeconds = 3600;

    }

}
