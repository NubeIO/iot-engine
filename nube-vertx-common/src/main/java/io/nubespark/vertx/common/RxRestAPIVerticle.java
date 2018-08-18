package io.nubespark.vertx.common;

import io.nubespark.utils.response.ResponseUtils;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.sstore.ClusteredSessionStore;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class RxRestAPIVerticle extends RxMicroServiceVerticle {
    private final String SESSION_NAME = "shopping.user.session";
    private Logger logger = LoggerFactory.getLogger(RxRestAPIVerticle.class);

    /**
     * Create http server for the REST service.
     *
     * @param router router instance
     * @param host   http host
     * @param port   http port
     * @return async result of the procedure
     */
    protected Single<HttpServer> createHttpServer(Router router, String host, int port) {
        return vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(port, host);
    }

    /**
     * Enable CORS support.
     *
     * @param router router instance
     */
    protected void enableCorsSupport(Router router) {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("Access-Control-Request-Method");
        allowHeaders.add("Access-Control-Allow-Credentials");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("Access-Control-Allow-Headers");
        allowHeaders.add("Content-Type");
        allowHeaders.add("origin");
        allowHeaders.add("x-requested-with");
        allowHeaders.add("accept");
        allowHeaders.add("X-PINGARUNER");
        allowHeaders.add("Authorization");
        allowHeaders.add("JSESSIONID");

        router.route().handler(CorsHandler.create("*")
                .allowedHeaders(allowHeaders)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.OPTIONS)
        );

    }

    /**
     * Enable local session storage in requests.
     *
     * @param router router instance
     */
    protected void enableLocalSession(Router router) {
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(
                LocalSessionStore.create(vertx, SESSION_NAME)));
    }

    /**
     * Enable clustered session storage in requests.
     *
     * @param router router instance
     */
    protected void enableClusteredSession(Router router) {
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(
                ClusteredSessionStore.create(vertx, SESSION_NAME)));
    }

    // Auth helper method

    /**
     * Validate if a user exists in the request scope.
     */
    protected void requireLogin(RoutingContext context, BiConsumer<RoutingContext, JsonObject> biHandler) {
        Optional<JsonObject> principal = Optional.ofNullable(context.request().getHeader("user-principal"))
                .map(JsonObject::new);
        if (principal.isPresent()) {
            biHandler.accept(context, principal.get());
        } else {
            context.response()
                    .setStatusCode(401)
                    .end(new JsonObject().put("message", "need_auth").encode());
        }
    }

    // helper result handler within a request context

    /**
     * This method generates handler for async methods in REST APIs.
     * Use the result directly and invoke `toString` as the response. The content type is JSON.
     */
    protected <T> SingleObserver<T> resultObserver(RoutingContext context) {
        return new DefaultSingleObserver<T>(context) {
            @Override
            public void onSuccess(T res) {
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(res == null ? "{}" : res.toString());
            }
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * Use the result directly and use given {@code converter} to convert result to string
     * as the response. The content type is JSON.
     *
     * @param context   routing context instance
     * @param converter a converter that converts result to a string
     * @param <T>       result type
     * @return generated handler
     */
    protected <T> SingleObserver<T> resultObserver(RoutingContext context, Function<T, String> converter) {
        return new DefaultSingleObserver<T>(context) {
            @Override
            public void onSuccess(T res) {
                if (res == null) {
                    serviceUnavailable(context, "invalid_result");
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(converter.apply(res));
                }
            }
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The result requires non-empty. If empty, return <em>404 Not Found</em> status.
     * The content type is JSON.
     *
     * @param context routing context instance
     * @param <T>     result type
     * @return generated handler
     */
    protected <T> SingleObserver<T> resultNonEmptyObserver(RoutingContext context) {
        return new DefaultSingleObserver<T>(context) {
            @Override
            public void onSuccess(T res) {
                if (res == null) {
                    notFound(context);
                } else {
                    context.response()
                            .putHeader("content-type", "application/json")
                            .end(res.toString());
                }
            }
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The content type is originally raw text.
     *
     * @param context routing context instance
     * @param <T>     result type
     * @return generated handler
     */
    protected <T> SingleObserver<T> rawResultObserver(RoutingContext context) {
        return new DefaultSingleObserver<T>(context) {
            @Override
            public void onSuccess(T res) {
                context.response()
                        .end(res == null ? "" : res.toString());
            }
        };
    }

    protected <T> SingleObserver<T> resultVoidObserver(RoutingContext context, JsonObject result) {
        return resultVoidObserver(context, result, 200);
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The result is not needed. Only the state of the async result is required.
     *
     * @param context routing context instance
     * @param result  result content
     * @param status  status code
     * @return generated handler
     */
    protected <T> SingleObserver<T> resultVoidObserver(RoutingContext context, JsonObject result, int status) {
        return new DefaultSingleObserver<T>(context) {
            @Override
            public void onSuccess(T res) {
                context.response()
                        .setStatusCode(status == 0 ? 200 : status)
                        .putHeader("content-type", "application/json")
                        .end(result.encodePrettily());
            }
        };
    }

    protected <T> SingleObserver<T> resultVoidObserver(RoutingContext context, int status) {
        return new DefaultSingleObserver<T>(context) {
            @Override
            public void onSuccess(T res) {
                context.response()
                        .setStatusCode(status == 0 ? 200 : status)
                        .putHeader("content-type", "application/json")
                        .end();
            }
        };
    }

    /**
     * This method generates handler for async methods in REST DELETE APIs.
     * Return format in JSON (successful status = 204):
     * <code>
     * {"message": "delete_success"}
     * </code>
     *
     * @param context routing context instance
     * @return generated handler
     */
    protected <T> SingleObserver<T> deleteResultObserver(RoutingContext context) {
        return new DefaultSingleObserver<T>(context) {
            @Override
            public void onSuccess(T res) {
                context.response().setStatusCode(204)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("message", "delete_success").encodePrettily());
            }
        };
    }

    // helper method dealing with failure
    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }

    protected void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_implemented").encodePrettily());
    }

    protected void badGateway(Throwable ex, RoutingContext context) {
        ex.printStackTrace();
        context.response()
                .setStatusCode(502)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "Bad Gateway")
                        .put("message", ex.getMessage())
                        .encodePrettily());
    }

    protected void serviceUnavailable(RoutingContext context) {
        context.fail(503);
    }

    protected void serviceUnavailable(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void serviceUnavailable(RoutingContext context, String cause) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", cause).encodePrettily());
    }

    protected void failAuthentication(RoutingContext ctx) {
        ctx.response().setStatusCode(401)
                .putHeader(ResponseUtils.CONTENT_TYPE, ResponseUtils.CONTENT_TYPE_JSON)
                .end(Json.encodePrettily(new JsonObject().put("message", "Unauthorized")));
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    class DefaultSingleObserver<T> implements SingleObserver<T> {

        private RoutingContext context;

        DefaultSingleObserver(RoutingContext context) {
            this.context = context;
        }

        @Override
        public void onSubscribe(Disposable disposable) {

        }

        @Override
        public void onSuccess(T t) {

        }

        @Override
        public void onError(Throwable throwable) {
            internalError(context, throwable);
            getLogger().error(throwable);
        }
    }
}
