package com.nubeiot.core.http.client;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HostInfo;

import lombok.NonNull;

/**
 * Due cache mechanism, before closing {@code Vertx}, it is mandatory to call {@link HttpClientRegistry#clear()}
 */
public interface HttpClientDelegate extends IClientDelegate {

    /**
     * Create new {@code HTTP client} by wrapping another {@code HTTP client}. It is used by {@code Service Discovery}
     * and not cached then after using, must close {@code HTTP client} explicit
     *
     * @param client HTTP Client instance
     * @return {@code HTTP client delegate}
     */
    static HttpClientDelegate create(@NonNull HttpClient client) {
        return new HttpClientDelegateImpl(client);
    }

    /**
     * Create new {@code HTTP client}
     *
     * @param vertx  Vertx
     * @param config HTTP Client config
     * @return {@code HTTP client delegate}
     */
    static HttpClientDelegate create(@NonNull Vertx vertx, JsonObject config) {
        return create(vertx, IConfig.parseConfig(config, HttpClientConfig.class, HttpClientConfig::new));
    }

    /**
     * Create new {@code HTTP client}
     *
     * @param vertx  Vertx
     * @param config HTTP Client config
     * @return {@code HTTP client delegate}
     */
    static HttpClientDelegate create(@NonNull Vertx vertx, @NonNull HttpClientConfig config) {
        return create(vertx, config, null);
    }

    /**
     * Create new {@code HTTP client} by default config and given host info
     *
     * @param vertx    Vertx
     * @param hostInfo Override {@code host}, {@code port}, {@code SSL} option in config
     * @return {@code HTTP client delegate}
     */
    static HttpClientDelegate create(@NonNull Vertx vertx, HostInfo hostInfo) {
        return create(vertx, new HttpClientConfig(), hostInfo);
    }

    /**
     * Create new {@code Websocket client} with {@code idle timeout} is {@link HttpClientConfig#WS_IDLE_TIMEOUT_SECOND}
     * seconds
     *
     * @param vertx    Vertx
     * @param config   HTTP Client config
     * @param hostInfo Override {@code host}, {@code port}, {@code SSL} option in config
     * @return {@code HTTP client delegate}
     */
    static HttpClientDelegate create(@NonNull Vertx vertx, @NonNull HttpClientConfig config, HostInfo hostInfo) {
        HttpClientConfig clone = ClientDelegate.cloneConfig(config, hostInfo, config.getOptions().getIdleTimeout());
        return HttpClientRegistry.getInstance()
                                 .getHttpClient(clone.getHostInfo(), () -> new HttpClientDelegateImpl(vertx, clone));
    }

    /**
     * Execute HTTP request
     *
     * @param path        Request path
     * @param method      Http Method
     * @param requestData Request data
     * @return single response data. Must be subscribe before using
     * @apiNote It is equivalent to call {@link #execute(String, HttpMethod, RequestData, boolean)} with {@code
     *     swallowError} is {@code true}
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

}
