package com.nubeiot.core.http.handler;

import java.nio.file.Path;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DownloadFileHandler implements Handler<RoutingContext> {

    private final String downloadPath;
    private final Path downloadDir;

    public static DownloadFileHandler create(String downloadPath, @NonNull Path downloadDir) {
        return new DownloadFileHandler(downloadPath, downloadDir);
    }

    @Override
    public void handle(RoutingContext context) {
        final String filePath = context.request().path().replaceFirst(downloadPath, "");
        context.response()
               .setChunked(true)
               .setStatusCode(HttpResponseStatus.OK.code())
               .sendFile(downloadDir.resolve(filePath).toString())
               .end();
    }

}
