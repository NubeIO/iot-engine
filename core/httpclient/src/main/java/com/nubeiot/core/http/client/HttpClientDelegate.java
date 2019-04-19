package com.nubeiot.core.http.client;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HostInfo;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import lombok.NonNull;

public interface HttpClientDelegate extends IClientDelegate {

    static HttpClientDelegate create(@NonNull HttpClient client) {
        return new HttpClientDelegateImpl(client);
    }

    static HttpClientDelegate create(@NonNull Vertx vertx, @NonNull HttpClientConfig config) {
        return HttpClientRegistry.getInstance()
                                 .getHttpClient(config.getHostInfo(), () -> new HttpClientDelegateImpl(vertx, config));
    }

    static HttpClientDelegate create(@NonNull Vertx vertx, HostInfo hostInfo) {
        return HttpClientRegistry.getInstance()
                                 .getHttpClient(hostInfo, () -> new HttpClientDelegateImpl(vertx,
                                                                                           ClientDelegate.cloneConfig(
                                                                                               new HttpClientConfig(),
                                                                                               hostInfo,
                                                                                               HttpClientConfig.HTTP_IDLE_TIMEOUT_SECOND)));
    }

    /**
     * Execute HTTP request
     *
     * @param path        Request path
     * @param method      Http Method
     * @param requestData Request data
     * @return single response data. Must be subscribe before using
     */
    default Single<ResponseData> execute(String path, HttpMethod method, RequestData requestData) {
        return this.execute(path, method, requestData, true);
    }

    /**
     * Execute HTTP request
     *
     * @param path         Request path
     * @param method       Http Method
     * @param requestData  Request data
     * @param swallowError Swallow error in {@link ResponseData} instead raise {@link Single#error(Throwable)} if {@code
     *                     HTTP Response status code >= 400}
     * @return single response data. Must be subscribe before using
     */
    Single<ResponseData> execute(String path, HttpMethod method, RequestData requestData, boolean swallowError);

    /**
     * Upload file in {@code POST} method
     *
     * @param path       Request path
     * @param uploadFile Absolute path for upload file
     * @return single response data. Must be subscribe before using
     * @see #upload(String, AsyncFile)
     */
    Single<ResponseData> upload(String path, String uploadFile);

    /**
     * Upload file in {@code POST} method
     *
     * @param uploadFile File
     * @return single response data. Must be subscribe before using
     * @see #push(ReadStream, HttpMethod)
     */
    default Single<ResponseData> upload(AsyncFile uploadFile) {
        return this.upload(null, uploadFile);
    }

    /**
     * Upload file in {@code POST} method
     *
     * @param path       Request path
     * @param uploadFile File
     * @return single response data. Must be subscribe before using
     * @see #push(String, ReadStream, HttpMethod)
     */
    default Single<ResponseData> upload(String path, AsyncFile uploadFile) {
        return this.push(path, uploadFile, HttpMethod.POST);
    }

    /**
     * Push data from read stream to server. It's useful when redirect data
     *
     * @param readStream Source stream
     * @param method     Http Method
     * @return single response data. Must be subscribe before using
     * @see #push(String, ReadStream, HttpMethod)
     */
    default Single<ResponseData> push(ReadStream readStream, HttpMethod method) {
        return this.push(null, readStream, HttpMethod.POST);
    }

    /**
     * Push data from read stream to server. It's useful when redirect data
     *
     * @param path       Request options. Override default server host and port
     * @param readStream Source stream
     * @param method     Http Method
     * @return single response data. Must be subscribe before using
     */
    Single<ResponseData> push(String path, ReadStream readStream, HttpMethod method);

    /**
     * Download data from server and save it to local file
     *
     * @param saveFile Save file
     * @return single async file a reference to {@code saveFile }parameter, so the API can be used fluently
     */
    default Single<AsyncFile> download(AsyncFile saveFile) {
        return this.download(null, saveFile);
    }

    /**
     * Download data from server and save it to local file
     *
     * @param path     Request path
     * @param saveFile Save file
     * @return single async file a reference to {@code saveFile }parameter, so the API can be used fluently
     */
    Single<AsyncFile> download(String path, AsyncFile saveFile);

    /**
     * Pull data from server then redirect it to destination stream
     *
     * @param writeStream destination stream
     * @return single {@code WriteStream} a reference to {@code saveFile }parameter, so the API can be used fluently
     */
    default Single<WriteStream> pull(WriteStream writeStream) {
        return this.pull(null, writeStream);
    }

    /**
     * Pull data from server then redirect it to destination stream
     *
     * @param path        Request path
     * @param writeStream destination stream
     * @return single {@code WriteStream} a reference to {@code saveFile }parameter, so the API can be used fluently
     */
    Single<WriteStream> pull(String path, WriteStream writeStream);

    HttpClientDelegate overrideEndHandler(Handler<Void> o);

}
