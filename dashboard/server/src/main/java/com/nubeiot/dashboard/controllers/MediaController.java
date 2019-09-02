package com.nubeiot.dashboard.controllers;

import static com.nubeiot.dashboard.constants.Collection.MEDIA_FILES;
import static com.nubeiot.dashboard.utils.FileUtils.appendRealFileNameWithExtension;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ext.mongo.MongoClient;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.handler.ResponseDataWriter;
import com.nubeiot.core.http.helper.ResponseDataHelper;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.mongo.MongoUtils;
import com.nubeiot.core.mongo.RestMongoClientProvider;
import com.nubeiot.dashboard.providers.RestMediaDirProvider;
import com.nubeiot.dashboard.utils.ResourceUtils;
import com.zandero.rest.annotation.RouteOrder;

@Path("/media_file")
public class MediaController implements RestApi {

    @GET
    @Path("/:id")
    @RouteOrder(3)
    public Future<ResponseData> get(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient,
                                    @Context RestMediaDirProvider mediaDirProvider) {
        return handleGetMediaFile(ctx, mongoClient.getMongoClient(), mediaDirProvider.getMediaDir());
    }

    @POST
    @Path("/")
    @RouteOrder(3)
    public Future<ResponseData> post(@Context RoutingContext ctx, @Context RestMongoClientProvider mongoClient,
                                     @Context RestMediaDirProvider mediaDirProvider) {

        return handlePostMediaFiles(ctx, mongoClient.getMongoClient(), mediaDirProvider);
    }

    private Future<ResponseData> handleGetMediaFile(RoutingContext ctx, MongoClient mongoClient, String mediaDir) {
        Future<ResponseData> future = Future.future();
        String id = ctx.request().getParam("id");
        mongoClient.rxFindOne(MEDIA_FILES, MongoUtils.idQuery(id), null).subscribe(record -> {
            if (record != null) {
                String file = record.getString("name");
                JsonObject body = new JsonObject();
                body.put("absolute_path", ResourceUtils.buildAbsolutePath(ctx.request().host(), mediaDir, file));
                future.complete(ResponseDataWriter.serializeResponseData(body.encode()));
            } else {
                future.complete(new ResponseData().setStatus(HttpResponseStatus.NOT_FOUND.code()));
            }
        }, throwable -> future.complete(ResponseDataHelper.internalServerError(throwable.getMessage())));
        return future;
    }

    private Future<ResponseData> handlePostMediaFiles(RoutingContext ctx, MongoClient mongoClient,
                                                      RestMediaDirProvider mediaDirProvider) {
        Future<ResponseData> future = Future.future();
        if (ctx.fileUploads().isEmpty()) {
            future.complete(new ResponseData().setStatus(HttpResponseStatus.BAD_REQUEST.code()));
            return future;
        }

        JsonObject output = new JsonObject();
        Observable.fromIterable(ctx.fileUploads())
                  .flatMapSingle(fileUpload -> {
                      String mediaAbsoluteDir = mediaDirProvider.getMediaAbsoluteDir();
                      String mediaDir = mediaDirProvider.getMediaDir();
                      String name = appendRealFileNameWithExtension(fileUpload).replace(mediaAbsoluteDir + "/", "");
                      String link = ResourceUtils.buildAbsolutePath(ctx.request().host(), mediaDir, name);
                      JsonObject mediaFile = new JsonObject().put("name", name).put("title", fileUpload.name());
                      return mongoClient.rxInsert(MEDIA_FILES, mediaFile)
                                        .map(id -> output.put(fileUpload.name(), id)
                                                         .put(fileUpload.name() + "_path", link));
                  })
                  .toList()
                  .subscribe(ignored -> future.complete(
                      ResponseDataWriter.serializeResponseData(output.encode()).setStatus(HttpResponseStatus.CREATED)),
                             throwable -> future.complete(
                                 ResponseDataHelper.internalServerError(throwable.getMessage())));
        return future;
    }

}
