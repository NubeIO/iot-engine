package com.nubeiot.core.http;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.http.base.HttpScheme;
import com.nubeiot.core.http.base.InvalidUrlException;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.handler.DownloadFileHandler;
import com.nubeiot.core.http.handler.UploadFileHandler;
import com.nubeiot.core.http.handler.UploadListener;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class HttpConfig implements IConfig {

    public static final String NAME = "__http__";
    private String host = "0.0.0.0";
    private int port = 8080;
    private String publicHost = "";
    private int publicPort = -1;
    private HttpScheme publicScheme = HttpScheme.HTTP;
    private int maxBodySizeMB = 2;

    @JsonProperty(value = ServerOptions.NAME)
    private ServerOptions options = (ServerOptions) new ServerOptions().setCompressionSupported(true)
                                                                       .setDecompressionSupported(true);
    @JsonProperty(value = RestConfig.NAME)
    private RestConfig restConfig = new RestConfig();
    @JsonProperty(value = WebsocketConfig.NAME)
    private WebsocketConfig websocketConfig = new WebsocketConfig();
    @JsonProperty(value = Http2Config.NAME)
    private Http2Config http2Cfg = new Http2Config();
    @JsonProperty(value = CorsOptions.NAME)
    private CorsOptions corsOptions = new CorsOptions();
    @JsonProperty(value = FileStorageConfig.NAME)
    private FileStorageConfig fileStorageConfig = new FileStorageConfig();
    @JsonProperty(value = StaticWebConfig.NAME)
    private StaticWebConfig staticWebConfig = new StaticWebConfig();

    static String computeRootApi(String rootApi, String fallback) {
        if (Strings.isBlank(rootApi)) {
            return fallback;
        }
        String root = Urls.combinePath(rootApi);
        if (!Urls.validatePath(root)) {
            throw new InvalidUrlException("Root API is not valid");
        }
        return root;
    }

    @Override
    public String name() { return NAME; }

    @Override
    public Class<? extends IConfig> parent() { return NubeConfig.AppConfig.class; }

    @JsonIgnore
    public String publicUrl() {
        return Urls.buildURL(publicScheme, publicHost, publicPort);
    }

    @NoArgsConstructor
    public static class ServerOptions extends HttpServerOptions implements IConfig {

        public static final String NAME = "__options__";

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

        @Override
        public JsonObject toJson() { return super.toJson(); }

    }


    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class RestConfig implements IConfig {

        public static final String NAME = "__rest__";

        private boolean enabled = true;
        private String rootApi = ApiConstants.ROOT_API_PATH;
        @JsonProperty(value = DynamicRouteConfig.NAME)
        private DynamicRouteConfig dynamicConfig = new DynamicRouteConfig();

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

        @Getter
        @Setter(value = AccessLevel.PACKAGE)
        @NoArgsConstructor(access = AccessLevel.PACKAGE)
        public static class DynamicRouteConfig implements IConfig {

            public static final String NAME = "__dynamic__";

            private boolean enabled = false;
            private String path = ApiConstants.DYNAMIC_API_PATH;

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return RestConfig.class; }

        }

    }


    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class WebsocketConfig implements IConfig {

        public static final String NAME = "__websocket__";
        private boolean enabled = false;
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
    @Setter(value = AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Http2Config implements IConfig {

        public static final String NAME = "__http2__";

        private boolean enabled = false;

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

    }


    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
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


    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class FileStorageConfig implements IConfig {

        public static final String NAME = "__files__";

        private boolean enabled = false;
        private boolean externalHandler = false;
        private String dir = "files";
        @JsonProperty(value = UploadConfig.NAME)
        private UploadConfig uploadConfig = new UploadConfig();
        @JsonProperty(value = DownloadConfig.NAME)
        private DownloadConfig downloadConfig = new DownloadConfig();

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return HttpConfig.class; }

        @Getter
        @Setter(value = AccessLevel.PACKAGE)
        @NoArgsConstructor(access = AccessLevel.PACKAGE)
        public static class UploadConfig implements IConfig {

            public static final String NAME = "__upload__";

            private boolean enabled = false;
            private String path = ApiConstants.ROOT_UPLOAD_PATH;
            private int maxBodySizeMB = 10;
            private String handlerClass = UploadFileHandler.class.getName();
            private String listenerAddress;
            private String listenerClass = UploadListener.class.getName();

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return FileStorageConfig.class; }

        }


        @Getter
        @Setter(value = AccessLevel.PACKAGE)
        @NoArgsConstructor(access = AccessLevel.PACKAGE)
        public static class DownloadConfig implements IConfig {

            public static final String NAME = "__download__";

            private boolean enabled = false;
            private String path = ApiConstants.ROOT_DOWNLOAD_PATH;
            private String handlerClass = DownloadFileHandler.class.getName();

            @Override
            public String name() { return NAME; }

            @Override
            public Class<? extends IConfig> parent() { return FileStorageConfig.class; }

        }

    }


    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class StaticWebConfig implements IConfig {

        public static final String NAME = "__static__";
        private boolean enabled = false;
        private String webRoot = ApiConstants.SAMPLE_PATH;

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return StaticWebConfig.class; }

    }

}
