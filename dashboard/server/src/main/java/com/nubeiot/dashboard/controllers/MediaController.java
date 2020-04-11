package com.nubeiot.dashboard.controllers;

import static com.nubeiot.dashboard.constants.Collection.MEDIA_FILES;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.github.zero.utils.HttpScheme;
import io.github.zero.utils.Strings;
import io.github.zero.utils.Urls;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.helper.ResponseDataHelper;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.provider.RestDownloadConfigProvider;
import com.nubeiot.core.http.rest.provider.RestHttpConfigProvider;
import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.zandero.rest.annotation.RouteOrder;

@Path("/media_file")
public class MediaController implements RestApi {

    @GET
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> get(@Context RoutingContext context, @Context RestMongoClientProvider mongoClient,
                                    @Context RestDownloadConfigProvider downloadConfigProvider,
                                    @Context RestHttpConfigProvider httpConfigProvider) {
        Future<ResponseData> future = Future.future();

        String id = context.request().getParam("id");
        String publicUrl = httpConfigProvider.getHttpConfig().publicServerUrl();

        mongoClient.getMongoClient().rxFindOne(MEDIA_FILES, MongoUtils.idQuery(id), null).subscribe(record -> {
            if (record != null) {
                String file = record.getString("name");
                JsonObject body = new JsonObject();
                String link = Strings.isBlank(publicUrl) ? Urls.buildURL(HttpScheme.parse(context.request().scheme()),
                                                                         context.request().host(), -1) : publicUrl;
                String downloadPath = downloadConfigProvider.getDownloadConfig().getPath();
                link = Strings.requireNotBlank(link, "Hostname can't be blank");
                body.put("absolute_path", link.concat(Urls.combinePath(downloadPath, file)));
                future.complete(ResponseDataWriter.serializeResponseData(body.encode()));
            } else {
                future.complete(new ResponseData().setStatus(HttpResponseStatus.NOT_FOUND.code()));
            }
        }, throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));

        return future;
    }

}
