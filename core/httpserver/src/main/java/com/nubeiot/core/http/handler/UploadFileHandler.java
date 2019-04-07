package com.nubeiot.core.http.handler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.http.base.HttpScheme;
import com.nubeiot.core.http.base.HttpUtils.HttpRequests;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Only override it if any performance issue
 */
@RequiredArgsConstructor
public class UploadFileHandler implements RestEventRequestDispatcher {

    @Getter
    private final EventController controller;
    private final EventModel eventModel;
    private final Path uploadDir;
    private final String publicUrl;

    public static UploadFileHandler create(String handlerClass, @NonNull EventController controller,
                                           @NonNull EventModel eventModel, @NonNull Path uploadDir, String publicUrl) {
        if (Strings.isBlank(handlerClass) || UploadFileHandler.class.getName().equals(handlerClass)) {
            return new UploadFileHandler(controller, eventModel, uploadDir, publicUrl);
        }
        Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(EventController.class, controller);
        inputs.put(EventModel.class, eventModel);
        inputs.put(Path.class, uploadDir);
        inputs.put(String.class, publicUrl);
        return ReflectionClass.createObject(handlerClass, inputs);
    }

    @Override
    public void handle(RoutingContext context) {
        if (context.fileUploads().isEmpty()) {
            context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            return;
        }
        String link = Strings.isBlank(publicUrl) ? Urls.buildURL(HttpScheme.parse(context.request().scheme()),
                                                                 context.request().host(), -1) : publicUrl;
        JsonObject data = new JsonObject();
        context.fileUploads().forEach(fileUpload -> data.put(fileUpload.name(), extractFileInfo(link, fileUpload)));
        data.put("attributes", HttpRequests.serializeHeaders(context.request().formAttributes()));
        EventMessage message = EventMessage.initial(
            eventModel.getEvents().stream().findFirst().orElse(EventAction.CREATE), data);
        dispatch(context, "UPLOAD", eventModel.getAddress(), eventModel.getPattern(), message);
    }

    private JsonObject extractFileInfo(String link, FileUpload fileUpload) {
        return new JsonObject().put("fileName", fileUpload.fileName())
                               .put("file", uploadDir.relativize(Paths.get(fileUpload.uploadedFileName())).toString())
                               .put("ext", FileUtils.getExtension(fileUpload.fileName()))
                               .put("charset", fileUpload.charSet())
                               .put("contentType", fileUpload.contentType())
                               .put("size", fileUpload.size())
                               .put("serverUrl", link);
    }

}
