package io.nubespark;

import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.KeycloakHelper;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.KeycloakAuth;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.HashMap;
import java.util.Map;

import static io.nubespark.utils.Constants.SERVER_DITTO_DRIVER;
import static io.nubespark.utils.Constants.SERVICE_NAME;

/**
 * Created by topsykretts on 5/4/18.
 */
public class HttpServerVerticle extends MicroServiceVerticle {

    private OAuth2Auth loginAuth;

    @Override
    public void start() {
        super.start();
        // TODO: 5/5/18 Implement backend logic of users, roles, authentication, business logic end points

        Router router = Router.router(vertx);
        // creating body handler
        router.route().handler(BodyHandler.create());

        // we are here enabling CORS, for QCing things from frontend
        handleEnableCors(router);
        handleAuth(router);
        handleDittoRESTFulRequest(router);
        handleEventBus(router);

        //creating static resource handler
        router.route().handler(StaticHandler.create());
        router.route("/*").handler(ctx -> ctx.response().sendFile("webroot/index.html"));

        String host = config().getString("host", "localhost");
        int port = config().getInteger("http.port", 8080);

        //By default index.html from webroot/ is available on "/".
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port,
                        handler -> {
                            if (handler.failed()) {
                                handler.cause().printStackTrace();
                                Future.failedFuture(handler.cause());
                            } else {
                                System.out.println("Front End server started on port: " + port);
                                Future.succeededFuture();
                            }
                        }
                );

        publishHttpEndpoint(SERVICE_NAME, host, port, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("Front End Server service published: " + ar.succeeded());
            }
        });

    }

    private void handleEnableCors(Router router) {
        // For testing server we make CORS available, we will comment out in production cluster
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type")
                .allowedHeader("origin")
                .allowedHeader("x-requested-with")
                .allowedHeader("accept")
                .allowedHeader("X-PINGARUNER")
                .allowedHeader("Authorization")
        );
    }

    private void handleAuth(Router router) {
        //login approach
        loginAuth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, config().getJsonObject("keycloak"));
        router.route("/api/login/account").handler((RoutingContext ctx) -> {
            JsonObject body = ctx.getBodyAsJson();
            String username = body.getString("username");
            String password = body.getString("password");

            loginAuth.authenticate(new JsonObject().put("username", username).put("password", password), res -> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    System.out.println(res.result());
                    ctx.fail(401);
                } else {
                    AccessToken token = (AccessToken) res.result();
                    ctx.response()
                            .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(token.principal()));
                }
            });
        });

        router.route("/api/*").handler(ctx -> {
            String authorization = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                authorization = authorization.substring("Bearer ".length());
                System.out.println(authorization);
                setAuthenticUser(ctx, authorization);
            } else {
                ctx.fail(401);
            }
        });

        router.route("/eventbus/*").handler(ctx ->
                setAuthenticUser(ctx, ctx.request().getParam("access_token")));

        router.route("/api/currentUser").handler(ctx -> {
            User user = ctx.user();
            if (user != null) {

                JsonObject accessToken = KeycloakHelper.accessToken(user.principal());

                String name = accessToken.getString("name", accessToken.getString("preferred_username"));

                //dummy for mock
                String avatar = "https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png";
                String userid = "00000001";
                Integer notifyCount = 12;
                ctx.response().putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                        .end(Json.encodePrettily(new JsonObject()
                                .put("name", name)
                                .put("avatar", avatar)
                                .put("userid", userid)
                                .put("notifyCount", notifyCount)
                        ));
            } else {
                System.out.println("Send not authorized error and user should login");
                ctx.fail(401);
            }
        });

        router.route("/api/logout").handler(this::redirectLogout);
    }

    private void handleDittoRESTFulRequest(Router router) {
        router.route("/api/2/*").handler(ctx -> {
            HttpServerRequest req = ctx.request();
            JsonObject request = new JsonObject();
            request.put("method", req.method().toString());
            request.put("uri", req.uri());
            Buffer body = ctx.getBody();
            if (body != null) {
                request.put("body", body.getBytes());
            }
            System.out.println(Json.encodePrettily(request));
            System.out.println("Sending response...");

            vertx.eventBus().<JsonObject>send(SERVER_DITTO_DRIVER, request, handler -> {
                if (handler.succeeded()) {
                    JsonObject response = handler.result().body();
                    JsonObject headers = response.getJsonObject("headers");
                    Map<String, String> headerMap = new HashMap<>();
                    for (String header : headers.fieldNames()) {
                        headerMap.put(header, headers.getString(header));
                    }
                    ctx.request().response()
                            .headers().setAll(headerMap);
                    ctx.request().response().setStatusCode(response.getInteger("statusCode"));
                    byte[] responseBody = response.getBinary("body");
                    if (responseBody != null) {
                        ctx.request().response().write(Buffer.buffer(responseBody));
                    }
                    ctx.request().response().end();
                } else {
                    // // TODO: 5/12/18 Identify cases where request fails and handle accordingly
                    ctx.request().response()
                            .setStatusCode(500)
                            .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(new JsonObject()
                                    .put("message", "Internal Server Error")
                                    .put("error", handler.cause().getMessage())
                            ));
                }
            });
        });
    }

    private void handleEventBus(Router router) {
        BridgeOptions options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("news-feed"))
                .addOutboundPermitted(new PermittedOptions().setAddress("io.nubespark.ditto.events"));

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {

            // You can also optionally provide a handler like this which will be passed any events that occur on the bridge
            // You can use this for monitoring or logging, or to change the raw messages in-flight.
            // It can also be used for fine grained access control.

            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                System.out.println("A socket was created");
            }

            // This signals that it's ok to process the event
            event.complete(true);

        }));
    }

    private void setAuthenticUser(RoutingContext ctx, String authorization) {
        loginAuth.introspectToken(authorization, res -> {
            if (res.succeeded()) {
                System.out.println("Auth Success");
                AccessToken token = res.result();
                ctx.setUser(token);
                ctx.next();
            } else {
                System.out.println("Auth Fail");
                res.cause().printStackTrace();
                ctx.fail(401);
            }
        });
    }

    private void redirectLogout(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        User user = ctx.user();
        String access_token = user.principal().getString("access_token");
        String refresh_token = body.getString("refresh_token");
        JsonObject keycloakConfig = config().getJsonObject("keycloak");
        String client_id = keycloakConfig.getString("resource");
        String client_secret = keycloakConfig
                .getJsonObject("credentials").getString("secret");
        String realmName = keycloakConfig.getString("realm");
        String uri = keycloakConfig.getString("auth-server-url")
                + "/realms/" + realmName + "/protocol/openid-connect/logout";

        HttpClient client = vertx.createHttpClient(new HttpClientOptions());

        HttpClientRequest request = client.requestAbs(HttpMethod.POST, uri, response -> {
            ctx.response().setStatusCode(response.statusCode()).end();
        });
        request.setChunked(true);

        String body$ = "refresh_token=" + refresh_token + "&client_id=" + client_id
                + "&client_secret=" + client_secret;
        request.putHeader("content-type", "application/x-www-form-urlencoded");
        request.putHeader("Authorization", "Bearer " + access_token);

        request.write(body$).end();
    }
}
