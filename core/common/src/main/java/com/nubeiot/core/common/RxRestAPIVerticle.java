package com.nubeiot.core.common;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.CorsHandler;

import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.micro.MicroContext;

@Deprecated
public interface RxRestAPIVerticle {

    /**
     * Enable CORS support.
     *
     * @param router router instance
     */
    default void enableCorsSupport(Router router) {
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
        allowHeaders.add("Site-Id");
        allowHeaders.add("Authorization");
        allowHeaders.add("JSESSIONID");

        router.route()
              .handler(CorsHandler.create("*")
                                  .allowedHeaders(allowHeaders)
                                  .allowedMethod(HttpMethod.GET)
                                  .allowedMethod(HttpMethod.PUT)
                                  .allowedMethod(HttpMethod.OPTIONS)
                                  .allowedMethod(HttpMethod.POST)
                                  .allowedMethod(HttpMethod.DELETE)
                                  .allowedMethod(HttpMethod.PATCH));
    }

    default Single<Buffer> dispatchRequests(MicroContext microContext, HttpMethod method, JsonObject headers,
                                            String path, JsonObject payload) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        int initialOffset = 5; // length of `/api/`
        if (path.length() <= initialOffset) {
            return Single.error(new HttpException(HttpResponseStatus.BAD_REQUEST, "Not found"));
        }
        String prefix = (path.substring(initialOffset).split("/"))[0];
        logger.info("Prefix: {}", prefix);
        String newPath = path.substring(initialOffset + prefix.length());
        logger.info("New path: {}", newPath);
        return microContext.getClusterController()
                           .executeHttpService(r -> prefix.equals(r.getMetadata().getString("api.name")), newPath,
                                               method, headers, payload);
    }

    default void dispatchRequests(MicroContext microContext, RoutingContext context, JsonObject settings) {
        HttpMethod method = context.request().method();
        JsonObject headers = new JsonObject();
        context.request().headers().getDelegate().forEach(header -> headers.put(header.getKey(), header.getValue()));
        headers.put("user", context.user().principal().toString());
        //TODO fix it
        headers.put("settings", Objects.isNull(settings) ? "{}" : settings.encode());
        JsonObject payload = (method == HttpMethod.DELETE || method == HttpMethod.GET) ? null : context.getBodyAsJson();
        dispatchRequests(microContext, method, headers, context.request().uri(), payload).subscribe(
            buffer -> handleResponse(context, buffer), t -> HttpHelper.badGateway(t, context));
    }

    default void handleResponse(RoutingContext context, Buffer buffer) {
        //        HttpServerResponse toRsp = context.response().setStatusCode(response.statusCode());
        //        response.headers().entries().forEach(header -> {
        //            if (!header.getKey().equals(HttpHeaders.TRANSFER_ENCODING.toString())) {
        //                toRsp.putHeader(header.getKey(), header.getValue());
        //            }
        //        });
        //        // send response
        //        toRsp.end(body);

        HttpServerResponse toRsp = context.response().setStatusCode(200);
        toRsp.end(buffer);
    }

}
