package com.nubeiot.core.http.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.MediaType;

import io.vertx.ext.web.Router;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.HttpConfig.DynamicRouteConfig;
import com.nubeiot.core.http.base.InvalidUrlException;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.handler.ApiExceptionHandler;
import com.nubeiot.core.http.handler.ApiJsonWriter;
import com.nubeiot.core.http.handler.RestEventResponseHandler;
import com.nubeiot.core.utils.Reflections;
import com.nubeiot.core.utils.Strings;
import com.zandero.rest.RestBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestApisBuilder {

    @NonNull
    private final Router router;
    @NonNull
    private final Set<Class<? extends RestApi>> restApiClass = new HashSet<>();
    @NonNull
    private final Set<Class<? extends RestEventApi>> restEventApiClass = new HashSet<>();
    private String rootApi = ApiConstants.ROOT_API_PATH;
    private DynamicRouteConfig dynamicRouteConfig;

    public RestApisBuilder registerApi(Collection<Class<? extends RestApi>> apiClass) {
        restApiClass.addAll(apiClass);
        return this;
    }

    public RestApisBuilder registerEventBusApi(Collection<Class<? extends RestEventApi>> eventBusApiClasses) {
        restEventApiClass.addAll(eventBusApiClasses);
        return this;
    }

    public RestApisBuilder rootApi(String rootApi) {
        if (Strings.isNotBlank(rootApi)) {
            String root = Urls.combinePath(rootApi);
            if (!Urls.validatePath(root)) {
                throw new InvalidUrlException("Root API is not valid");
            }
            this.rootApi = root;
        }
        return this;
    }

    public RestApisBuilder dynamicRouteConfig(DynamicRouteConfig dynamicRouteConfig) {
        this.dynamicRouteConfig = dynamicRouteConfig;
        return this;
    }

    public Router build() {
        if (restApiClass.isEmpty() && restEventApiClass.isEmpty() && !dynamicRouteConfig.isEnabled()) {
            throw new InitializerError("No REST API given, register at least one.");
        }
        String wildCards = Urls.combinePath(rootApi, ApiConstants.WILDCARDS_ANY_PATH);
        Router router = initRestApiRouter(initEventBusApiRouter(initDynamicRouter(this.router)));
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
        return new RestEventApisBuilder(router).rootApi(rootApi).register(restEventApiClass).build();
    }

    private Router initDynamicRouter(Router router) {
        if (!dynamicRouteConfig.isEnabled()) {
            return router;
        }
        try {
            Class.forName("com.nubeiot.core.micro.MicroContext", false, Reflections.contextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InitializerError("To enabled dynamic route, you have to put on nube-core-micro.jar in classpath",
                                       e);
        }
        router.route(Urls.combinePath(rootApi, dynamicRouteConfig.getPath(), ApiConstants.WILDCARDS_ANY_PATH))
              .disable();
        return router;
    }

}
