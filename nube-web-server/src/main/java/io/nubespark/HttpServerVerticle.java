package io.nubespark;

import io.nubespark.utils.response.ResponseUtils;
import io.nubespark.vertx.common.MicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
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
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * Created by topsykretts on 5/4/18.
 */
public class HttpServerVerticle extends MicroServiceVerticle {

    public static final String SERVICE_NAME = "io.nubespark.frontend.server";
    private OAuth2Auth loginAuth;

    @Override
    public void start() {
        super.start();
        //// TODO: 5/4/18 Use SockJs to make event bus available

        //// TODO: 5/5/18 Implement backend logic of users, roles, authentication, business logic end points

        Router router = Router.router(vertx);
        // creating body handler
        router.route().handler(BodyHandler.create());

        handleAuth(router);

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

        //creating static resource handler
        router.route().handler(StaticHandler.create());

        //By default index.html from webroot/ is available on "/".
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8080),
                        handler -> {
                            if (handler.failed()) {
                                handler.cause().printStackTrace();
                                Future.failedFuture(handler.cause());
                            } else {
                                System.out.println("Front End server started...");
                                Future.succeededFuture();
                            }
                        }
                );
        publishHttpEndpoint(SERVICE_NAME, config().getString("host", "localhost"),
                config().getInteger("http.port", 8080), ar-> {
                    if (ar.failed()) {
                        ar.cause().printStackTrace();
                    } else {
                        System.out.println("Front End Server service published : " + ar.succeeded());
                    }
                });

    }

    private void handleAuth(Router router) {
        //login approach
        loginAuth = KeycloakAuth.create(vertx, OAuth2FlowType.PASSWORD, config().getJsonObject("keycloak"));
        router.route("/api/login/account").handler((RoutingContext ctx) -> {

            System.out.println("Handling login..");

            JsonObject body = ctx.getBodyAsJson();
            String username = body.getString("username");
            String password = body.getString("password");

            loginAuth.authenticate(new JsonObject().put("username", username).put("password", password), res-> {
                if (res.failed()) {
                    res.cause().printStackTrace();
                    System.out.println(res.result());
                    ctx.fail(401);
                } else {
                    System.out.println("Login Success");
                    AccessToken token = (AccessToken) res.result();
                    System.out.println(token.principal());
                    ctx.response()
                            .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                            .end(Json.encodePrettily(token.principal()));
                }
            });
        });

        router.route("/api/*").handler(ctx -> {
            String authorization = ctx.request().getHeader(HttpHeaders.AUTHORIZATION);
            System.out.println(authorization);
            if (authorization != null) {
                authorization = authorization.substring("Bearer ".length());
                System.out.println(authorization);
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
            } else {
                System.out.println("No Authorization Header");
                ctx.fail(401);
            }
        });

        router.route("/api/currentUser").handler(ctx -> {
            System.out.println("Inside currentUser");
            User user = ctx.user();
            if(user != null) {

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
                //todo
                System.out.println("Send not authorized error and user should login");
                ctx.fail(401);
//                ctx.response().putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
//                        .end(Json.encodePrettily(new JsonObject("{\"name\":\"Serati Ma\",\"avatar\":\"https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png\",\"userid\":\"00000001\",\"notifyCount\":12}")));

            }
        });

        router.route("/api/logout").handler(ctx -> {
            User user = ctx.user();
            if (user != null) {
                AccessToken token = (AccessToken) user;
                token.revoke("access_token", res-> {
                    if (res.failed()) {
                        res.cause().printStackTrace();
                        ctx.fail(503);
                    } else {
                        token.revoke("refresh_token", res1-> {
                            if (res1.failed()) {
                                res1.cause().printStackTrace();
                            }
                            ctx.response().setStatusCode(204).end();
                        });
                    }
                });
            } else {
                System.out.println("User is not logged in");
                ctx.fail(400);
            }
        });
    }
}
