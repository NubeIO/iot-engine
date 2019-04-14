package com.nubeiot.core.http.client;

import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;

import lombok.NonNull;

public interface HttpClientDelegate extends Supplier<HttpClient> {

    static HttpClientDelegate create(@NonNull HttpClient client) {
        return new HttpClientDelegateImpl(client);
    }

    static HttpClientDelegate create(@NonNull Vertx vertx) {
        return new HttpClientDelegateImpl(vertx, null);
    }

    static HttpClientDelegate create(@NonNull Vertx vertx, HttpClientConfig config) {
        return new HttpClientDelegateImpl(vertx, config);
    }

    /**
     * @return null if use {@link #create(HttpClient)}
     */
    HttpClientConfig getConfig();

    /**
     * Execute HTTP request
     *
     * @param path        Server path. Server host and port will reuse from {@link #getConfig()}
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
     * @param path         Server path. Server host and port will reuse from {@link #getConfig()}
     * @param method       Http Method
     * @param requestData  Request data
     * @param swallowError Swallow error in {@link ResponseData} instead raise {@link Single#error(Throwable)} if {@code
     *                     HTTP Response status code >= 400}
     * @return single response data. Must be subscribe before using
     */
    default Single<ResponseData> execute(String path, HttpMethod method, RequestData requestData,
                                         boolean swallowError) {
        return this.execute(ClientDelegate.evaluateRequestOpts(getConfig(), path, requestData), method, requestData,
                            swallowError);
    }

    /**
     * Execute HTTP request
     *
     * @param options     Request options. Override default server host and port
     * @param method      Http Method
     * @param requestData Request data
     * @return single response data. Must be subscribe before using
     */
    default Single<ResponseData> execute(RequestOptions options, HttpMethod method, RequestData requestData) {
        return this.execute(options, method, requestData, true);
    }

    /**
     * Execute HTTP request
     *
     * @param options      Request options. Override default server host and port
     * @param method       Http Method
     * @param requestData  Request data
     * @param swallowError Swallow error if {@code Response HTTP status code >= 400}
     * @return single response data. Must be subscribe before using
     */
    Single<ResponseData> execute(RequestOptions options, HttpMethod method, RequestData requestData,
                                 boolean swallowError);

    /**
     * Upload file in {@code POST} method
     *
     * @param options    Request options. Override default server host and port
     * @param uploadFile Absolute path for upload file
     * @return single response data. Must be subscribe before using
     * @see #upload(RequestOptions, AsyncFile)
     */
    Single<ResponseData> upload(RequestOptions options, String uploadFile);

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
     * @param options    Request options
     * @param uploadFile File
     * @return single response data. Must be subscribe before using
     * @see #push(RequestOptions, ReadStream, HttpMethod)
     */
    default Single<ResponseData> upload(RequestOptions options, AsyncFile uploadFile) {
        return this.push(options, uploadFile, HttpMethod.POST);
    }

    /**
     * Push data from read stream to server. It's useful when redirect data
     *
     * @param readStream Source stream
     * @param method     Http Method
     * @return single response data. Must be subscribe before using
     * @see #push(RequestOptions, ReadStream, HttpMethod)
     */
    default Single<ResponseData> push(ReadStream readStream, HttpMethod method) {
        return this.push(null, readStream, HttpMethod.POST);
    }

    /**
     * Push data from read stream to server. It's useful when redirect data
     *
     * @param options    Request options. Override default server host and port
     * @param readStream Source stream
     * @param method     Http Method
     * @return single response data. Must be subscribe before using
     */
    Single<ResponseData> push(RequestOptions options, ReadStream readStream, HttpMethod method);

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
     * @param options  Request options. Override default server host and port
     * @param saveFile Save file
     * @return single async file a reference to {@code saveFile }parameter, so the API can be used fluently
     */
    Single<AsyncFile> download(RequestOptions options, AsyncFile saveFile);

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
     * @param options     Request options. Override default server host and port
     * @param writeStream destination stream
     * @return single {@code WriteStream} a reference to {@code saveFile }parameter, so the API can be used fluently
     */
    Single<WriteStream> pull(RequestOptions options, WriteStream writeStream);

    HttpClientDelegate overrideEndHandler(Handler<Void> o);

}
