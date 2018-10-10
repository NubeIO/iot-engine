package io.nubespark.vertx.common;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.nubespark.vertx.common.HttpHelper.badGateway;

public class RxRestAPIVerticle extends RxMicroServiceVerticle {
    @Override
    protected Logger getLogger() {
        return null;
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
            .allowedMethod(HttpMethod.PUT)
            .allowedMethod(HttpMethod.OPTIONS)
            .allowedMethod(HttpMethod.POST)
            .allowedMethod(HttpMethod.DELETE)
            .allowedMethod(HttpMethod.PATCH));
    }

    protected void dispatchRequests(RoutingContext context) {
        System.out.println("Dispatch Requests called");
        int initialOffset = 5; // length of `/api/`
        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> {
            getAllEndpoints().setHandler(ar -> {
                if (ar.succeeded()) {
                    List<Record> recordList = ar.result();
                    // get relative path and retrieve prefix to dispatch client
                    String path = context.request().uri();

                    if (path.length() <= initialOffset) {
                        HttpHelper.notFound(context);
                        future.complete();
                        return;
                    }
                    String prefix = (path.substring(initialOffset)
                        .split("/"))[0];
                    System.out.println("prefix = " + prefix);
                    // generate new relative path
                    String newPath = path.substring(initialOffset + prefix.length());
                    // get one relevant HTTP client, may not exist
                    System.out.println("new path = " + newPath);
                    Optional<Record> client = recordList.stream()
                        .filter(record -> record.getMetadata().getString("api.name") != null)
                        .filter(record -> record.getMetadata().getString("api.name").equals(prefix))
                        .findAny(); // simple load balance

                    if (client.isPresent()) {
                        System.out.println("Found client for uri: " + path);
                        Single<HttpClient> httpClientSingle = HttpEndpoint.rxGetClient(discovery,
                            rec -> rec.getType().equals(io.vertx.servicediscovery.types.HttpEndpoint.TYPE) && rec.getMetadata().getString("api.name").equals(prefix));
                        doDispatch(context, newPath, httpClientSingle, future);
                    } else {
                        System.out.println("Client endpoint not found for uri " + path);
                        HttpHelper.notFound(context);
                        future.complete();
                    }
                } else {
                    future.fail(ar.cause());
                }
            });
        }).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });
    }

    /**
     * Dispatch the request to the downstream REST layers.
     *
     * @param context          routing context instance
     * @param path             relative path
     * @param httpClientSingle relevant HTTP client
     */
    private void doDispatch(RoutingContext context, String path, Single<HttpClient> httpClientSingle, io.vertx.reactivex.core.Future<Object> cbFuture) {
        httpClientSingle.subscribe(client -> {
            HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response().setStatusCode(response.statusCode());
                            response.headers().getDelegate().forEach(header -> {
                                toRsp.putHeader(header.getKey(), header.getValue());
                            });
                            // send response
                            toRsp.end(body);
                            client.close();
                            cbFuture.complete();
                        }
                        io.vertx.servicediscovery.ServiceDiscovery.releaseServiceObject(discovery.getDelegate(), client);
                    });
                });
            // set headers
            context.request().headers().getDelegate().forEach(header -> {
                toReq.putHeader(header.getKey(), header.getValue());
            });
            if (context.getBody() == null) {
                toReq.end();
            } else {
                toReq.end(context.getBody());
            }
        });
    }

    /**
     * Get all REST endpoints from the service discovery infrastructure.
     *
     * @return async result
     */
    private io.vertx.reactivex.core.Future<List<Record>> getAllEndpoints() {
        io.vertx.reactivex.core.Future<List<Record>> future = io.vertx.reactivex.core.Future.future();
        discovery.getRecords(record -> record.getType().equals(io.vertx.servicediscovery.types.HttpEndpoint.TYPE),
            future.completer());
        return future;
    }
}
