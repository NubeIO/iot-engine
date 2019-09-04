package com.nubeiot.dashboard;

import static com.nubeiot.dashboard.ShareableMongoClient.SHARABLE_MONGO_CLIENT_DATA_KEY;
import static com.nubeiot.dashboard.ShareableMongoClient.SHARABLE_MONGO_CLIENT_SHARED_KEY;
import static com.nubeiot.dashboard.constants.Collection.MEDIA_FILES;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.http.handler.UploadFileHandler;

public class DashboardUploadFileHandler extends UploadFileHandler {

    public DashboardUploadFileHandler(Vertx vertx, EventController controller, EventModel eventModel, Path uploadDir,
                                      String publicUrl) {
        super(vertx, controller, eventModel, uploadDir, publicUrl);
    }

    @Override
    public void handle(RoutingContext context) {
        if (context.fileUploads().isEmpty()) {
            context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            return;
        }
        ShareableMongoClient shareableMongoClient = SharedDataDelegate.getLocalDataValue(getVertx(),
                                                                                         SHARABLE_MONGO_CLIENT_SHARED_KEY,
                                                                                         SHARABLE_MONGO_CLIENT_DATA_KEY);
        JsonObject data = new JsonObject();
        Observable.fromIterable(context.fileUploads()).flatMapSingle(fileUpload -> {
            String name = getUploadDir().relativize(Paths.get(fileUpload.uploadedFileName())).toString();
            JsonObject mediaFile = new JsonObject().put("name", name).put("title", fileUpload.name());
            return shareableMongoClient.getMongoClient()
                                       .rxInsert(MEDIA_FILES, mediaFile)
                                       .map(id -> data.put(fileUpload.name(), id));
        }).toList().subscribe(ignored -> {
            EventMessage message = EventMessage.initial(
                getEventModel().getEvents().stream().findFirst().orElse(EventAction.CREATE), data);
            dispatch(context, "UPLOAD", getEventModel().getAddress(), getEventModel().getPattern(), message);
        });
    }

}
