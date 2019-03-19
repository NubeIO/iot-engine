package com.nubeiot.core.http.gateway;

import java.util.Objects;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.HttpServer;
import com.nubeiot.core.http.ServerInfo;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.handler.DynamicContextDispatcher;
import com.nubeiot.core.http.rest.DynamicRestApi;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.micro.ServiceGatewayAnnounceMonitor;

public class RouteGatewayAnnounceHandler extends ServiceGatewayAnnounceMonitor {

    public RouteGatewayAnnounceHandler(Vertx vertx, ServiceDiscoveryController controller, String sharedKey) {
        super(vertx, controller, sharedKey);
    }

    @Override
    protected void handle(Record record) {
        DynamicRestApi api = DynamicRestApi.create(record);
        if (Objects.isNull(api)) {
            return;
        }
        final ServerInfo serverInfo = SharedDataDelegate.getLocalDataValue(getVertx(), getSharedKey(),
                                                                           HttpServer.SERVER_INFO_DATA_KEY);
        Router router = serverInfo.getRouter();
        final String path = Urls.combinePath(serverInfo.getApiPath(), serverInfo.getServicePath(), api.path(),
                                             ApiConstants.WILDCARDS_ANY_PATH);
        if (record.getStatus() == Status.UP) {
            logger.info("Enable dynamic route | Service: {} --- {}", path, api.name());
            router.route(path).handler(DynamicContextDispatcher.create(api, getController())).enable();
        } else {
            logger.info("Disable dynamic route | Service: {} --- {}", path, api.name());
            router.route(path).disable();
        }
    }

}
