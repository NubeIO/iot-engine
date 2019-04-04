package com.nubeiot.dashboard.connector.zeppelin;

import static com.nubeiot.core.http.handler.ResponseDataWriter.responseData;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.RestConfigProvider;
import com.nubeiot.core.http.converter.ResponseDataConverter;
import com.nubeiot.core.http.rest.RestApi;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpClientResponse;

public class ZeppelinRestController implements RestApi {

    private String COOKIE_NAME = "JSESSIONID";

    @GET
    @Path("/info")
    public JsonObject info(@Context RoutingContext ctx) {
        return new JsonObject().put("name", "zeppelin-verticle")
            .put("version", "1.0")
            .put("vert.x_version", "3.4.1")
            .put("java_version", "8.0");
    }

    @POST
    @Path("/api/*")
    public Future<ResponseData> engine(@Context io.vertx.core.Vertx vertx, @Context RoutingContext ctx,
                                       @Context RestConfigProvider config) {

        ZeppelinConfig zeppelinConfig = IConfig.from(config.getConfig().getAppConfig(), ZeppelinConfig.class);
        return dispatchRequests(new Vertx(vertx), zeppelinConfig, ctx);
    }

    private Future<ResponseData> dispatchRequests(Vertx vertx, ZeppelinConfig zeppelinConfig, RoutingContext ctx) {
        Future<ResponseData> future = Future.future();
        ResponseData responseData = new ResponseData();
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());
        JsonObject zeppelinConfigJson = zeppelinConfig.toJson();
        HttpClientRequest req = client.request(ctx.request().method(), zeppelinConfigJson.getInteger("port"),
                                               zeppelinConfigJson.getString("host"), ctx.request().uri());

        req.handler(res -> res.bodyHandler(body -> {
            if (res.statusCode() < 500) {
                cookieHandler(res, body, responseData);
            } else {
                responseData(responseData, body.toString());
            }
            responseData.setStatus(res.statusCode());
            future.complete(responseData);
        })).exceptionHandler(e -> future.complete(ResponseDataConverter.convert(e)));

        // set headers
        ctx.request().headers().forEach(header -> {
            if (header.getKey().equalsIgnoreCase(COOKIE_NAME)) {
                // Sending as an cookie for authentication parameter
                req.putHeader("Cookie", header.getValue());
            } else {
                req.putHeader(header.getKey(), header.getValue());
            }
        });

        if (ctx.getBody() == null) {
            req.end();
        } else {
            req.end(ctx.getBody().toString());
        }
        return future;
    }

    @SuppressWarnings("Duplicates")
    private void cookieHandler(HttpClientResponse res, Buffer body, ResponseData responseData) {
        // Ignore cookies; on login it will send cookie
        JsonObject headers = JsonObject.mapFrom(res.headers().remove("Set-Cookie"));

        final Map<String, String> cookie = new HashMap<>();

        res.headers().getDelegate().forEach(header -> {
            if (header.getKey().equalsIgnoreCase("Set-Cookie")) {
                if (header.getValue().contains(COOKIE_NAME)) {
                    // Two JSESSIONID= will be available and we need the last one
                    cookie.put(COOKIE_NAME, (header.getValue().split(";")[0]).split("=")[1]);
                }
            }
        });

        if (body.length() != 0) {
            if (cookie.keySet().contains(COOKIE_NAME)) {
                JsonObject responseJsonObject = new JsonObject(body.getDelegate());
                JsonObject body$ = responseJsonObject.getJsonObject("body").put(COOKIE_NAME, cookie.get(COOKIE_NAME));
                responseJsonObject = responseJsonObject.put("body", body$);
                body = Buffer.buffer(responseJsonObject.toString());
                headers.put("Content-Length", Integer.toString(body.toString().toCharArray().length));
            }
        }

        responseData.setHeaders(headers);
        responseData(responseData, body.toString());
    }

}
