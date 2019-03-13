package com.nubeiot.core.http.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.MediaType;

import io.vertx.ext.web.Router;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.InvalidUrlException;
import com.nubeiot.core.http.handler.ApiExceptionHandler;
import com.nubeiot.core.http.handler.ApiJsonWriter;
import com.nubeiot.core.http.handler.RestEventResponseHandler;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.utils.Strings;
import com.zandero.rest.RestBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestApiBuilder {

    @NonNull
    private final Router router;
    @NonNull
    private final Set<Class<? extends RestApi>> restApiClass = new HashSet<>();
    @NonNull
    private final Set<Class<? extends RestEventApi>> restEventApiClass = new HashSet<>();
    private String rootApi = ApiConstants.ROOT_API_PATH;

    public RestApiBuilder registerApi(Collection<Class<? extends RestApi>> apiClass) {
        restApiClass.addAll(apiClass);
        return this;
    }

    public RestApiBuilder registerEventBusApi(Collection<Class<? extends RestEventApi>> eventBusApiClasses) {
        restEventApiClass.addAll(eventBusApiClasses);
        return this;
    }

    public RestApiBuilder rootApi(String rootApi) {
        if (Strings.isNotBlank(rootApi)) {
            String root = Urls.combinePath(rootApi);
            if (!Urls.validatePath(root)) {
                throw new InvalidUrlException("Root API is not valid");
            }
            this.rootApi = root;
        }
        return this;
    }

    public Router build() {
        if (restApiClass.isEmpty() && restEventApiClass.isEmpty()) {
            throw new InitializerError("No REST API given, register at least one.");
        }
        String wildCards = Urls.combinePath(rootApi, ApiConstants.WILDCARDS_ANY_PATH);
        Router router = initRestApiRouter(initEventBusApiRouter(this.router));
        router.route(wildCards).handler(new RestEventResponseHandler()).produces(ApiConstants.DEFAULT_CONTENT_TYPE);
        return router;
    }

    private Router initRestApiRouter(Router router) {
        if (restApiClass.isEmpty()) {
            return router;
        }
        Class[] classes = restApiClass.toArray(new Class[] {});
        return new RestBuilder(router).errorHandler(ApiExceptionHandler.class)
                                      .writer(MediaType.APPLICATION_JSON_TYPE, ApiJsonWriter.class)
                                      .register((Object[]) classes)
                                      .build();
    }

    private Router initEventBusApiRouter(Router router) {
        if (restEventApiClass.isEmpty()) {
            return router;
        }
        return new RestEventBuilder(router).rootApi(rootApi).register(restEventApiClass).build();
    }

}
