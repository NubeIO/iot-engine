package com.nubeiot.dashboard.connector.ditto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.HttpConfig;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.utils.Strings;
import com.zandero.rest.annotation.ResponseWriter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

@SuppressWarnings("Duplicates")
@Path("/api/ditto")
public class ServerDittoRestController implements RestApi {

    private static final Logger logger = LoggerFactory.getLogger(ServerDittoRestController.class);

    @GET
    @Path("/info")
    public JsonObject info(@Context RoutingContext ctx) {
        return new JsonObject().put("name", "server-ditto-driver")
                               .put("version", "1.0")
                               .put("vert.x_version", "3.4.1")
                               .put("java_version", "8.0");
    }

    @GET
    @Path("/api/*")
    @ResponseWriter(ResponseDataWriter.class)
    public Future<ResponseData> dittoGet(@Context ServerDittoDriver.ConfigProvider config, @Context Vertx vertx,
                                         @Context RoutingContext ctx) {
        return dittoRequestDispatcher(vertx, ctx, config);
    }

    @POST
    @Path("/api/*")
    public Future<ResponseData> dittoPost(@Context ServerDittoDriver.ConfigProvider config, @Context Vertx vertx,
                                         @Context RoutingContext ctx) {
        return dittoRequestDispatcher(vertx, ctx, config);
    }

    @PUT
    @Path("/api/*")
    public Future<ResponseData> dittoPut(@Context ServerDittoDriver.ConfigProvider config, @Context Vertx vertx,
                                         @Context RoutingContext ctx) {
        return dittoRequestDispatcher(vertx, ctx, config);
    }

    @DELETE
    @Path("/api/*")
    public Future<ResponseData> dittoDelete(@Context ServerDittoDriver.ConfigProvider config, @Context Vertx vertx,
                                         @Context RoutingContext ctx) {
        return dittoRequestDispatcher(vertx, ctx, config);
    }

    private Future<ResponseData> dittoRequestDispatcher(Vertx vertx, RoutingContext ctx,
                                                        ServerDittoDriver.ConfigProvider config) {
        Future<ResponseData> future = Future.future();
        HttpClient client = vertx.createHttpClient(
            new HttpClientOptions().setVerifyHost(false).setTrustAll(true).setTcpKeepAlive(true));

        DittoConfig dittoConfig = IConfig.from(config.getConfig(), DittoConfig.class);
        HttpConfig httpConfig = IConfig.from(config.getConfig(), HttpConfig.class);
        // Getting actual Ditto call API
        String uri = ctx.request().uri().replaceFirst(httpConfig.getRootApi(), "");
        logger.info("Proxying request: {}", uri);

        HttpMethod httpMethod = ctx.request().method();
        String host = dittoConfig.getHost();
        int port = dittoConfig.getPort();
        boolean ssl = false;
        if (port == 443 || port == 8443) {
            ssl = true;
        }
        HttpClientRequest req = client.request(httpMethod, new RequestOptions().setHost(host)
                                                                               .setPort(port)
                                                                               .setURI(uri)
                                                                               .setSsl(ssl));

        ResponseData responseData = new ResponseData();
        req.handler(res -> {
            logger.info("Proxying Response StatusCode: {}", res.statusCode());
            responseData.setHeaders(new JsonObject().put("statusCode", res.statusCode()));
            Buffer data = new BufferImpl();
            res.handler(x -> data.appendBytes(x.getBytes())).endHandler((v) -> {
                responseData.setBody(new JsonObject().put("message", new String(data.getBytes(), UTF_8)));
                logger.info("Proxy Response Completed.");
                future.complete(responseData);
            });
        }).exceptionHandler(e -> {
            responseData.setHeaders(new JsonObject().put("statusCode", 500));
            responseData.setBody(new JsonObject().put("message", e.getMessage()));
            future.complete(responseData);
        });

        req.setChunked(true);

        //Adding ditto authorization
        if (dittoConfig.getPolicy()) {
            req.putHeader(HttpHeaders.AUTHORIZATION.toString(),
                          ctx.request().headers().get(HttpHeaders.AUTHORIZATION.toString()));
            if (Strings.isNotBlank(ctx.getBody().toString())) {
                if (Strings.isBlank(uri.replaceAll("/api/2/things/[^/]*(/)?", ""))) {
                    // This means we are we are PUTing device value for the first time or going to updated whole data
                    JsonObject body = ctx.getBodyAsJson();
                    body.put("policyId", dittoConfig.getPrefix() + ":" +
                                         new JsonObject(ctx.request().headers().get("User")).getString("site_id"));
                    req.write(body.toString());
                } else {
                    req.write(ctx.getBody());
                }
            }
        } else {
            req.putHeader(HttpHeaders.AUTHORIZATION.toString(),
                          "Basic " + getAuthKey(dittoConfig.getUsername(), dittoConfig.getPassword()));
            if (Strings.isNotBlank(ctx.getBody().toString())) {
                req.write(ctx.getBody());
            }
        }
        req.end();

        return future;
    }

    private String getAuthKey(String username, String password) {
        String auth = username + ":" + password;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

}
