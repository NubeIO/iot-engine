package com.nubeiot.core.http.gateway;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

/**
 * Represents listener for event after any service up/down automatically
 * <p>
 * To use it:
 * <ul>
 * <li>Add {@code nube-core-micro} in classpath</li>
 * <li>Modify {@code NubeConfig} in json file then override it in {@code Microservice} config. For example:
 * <pre>
 * {
 *   "__app__": {
 *     "__micro__": {
 *       "__gateway__": {
 *         "enabled": true,
 *         "localAnnounceMonitorClass": "com.nubeiot.core.http.gateway.RouterAnnounceListener"
 *       },
 *       "__serviceDiscovery__": {
 *         "enabled": false
 *       },
 *       "__localServiceDiscovery__": {
 *         "enabled": true
 *       },
 *       "__circuitBreaker__": {
 *         "enabled": true
 *       }
 *     },
 *     "__http__": {
 *       "__dynamic__": {
 *         "enabled": true,
 *         "path": "/s"
 *       }
 *     }
 *   }
 * }
 * </pre>
 * </li>
 * </ul>
 */
public class RouterAnnounceListener extends ServiceGatewayAnnounceMonitor {

    public RouterAnnounceListener(Vertx vertx, ServiceDiscoveryController controller, String sharedKey) {
        super(vertx, controller, sharedKey);
    }

    @Override
    protected void handle(Record record) {
        DynamicRestApi api = DynamicRestApi.create(record);
        if (Objects.isNull(api)) {
            return;
        }
        ServerInfo serverInfo = SharedDataDelegate.getLocalDataValue(getVertx(), getSharedKey(),
                                                                     HttpServer.SERVER_INFO_DATA_KEY);
        Router router = serverInfo.getRouter();
        String gatewayPath = Urls.combinePath(serverInfo.getApiPath(), serverInfo.getServicePath());
        List<String> paths = api.alternativePaths()
                                .orElse(Collections.singleton(api.path()))
                                .stream()
                                .map(p -> Urls.combinePath(gatewayPath, p,
                                                           api.path().equals(p) ? ApiConstants.WILDCARDS_ANY_PATH : ""))
                                .sorted(Comparator.reverseOrder())
                                .collect(Collectors.toList());
        if (record.getStatus() == Status.UP) {
            DynamicContextDispatcher<DynamicRestApi> handler = DynamicContextDispatcher.create(api, gatewayPath,
                                                                                               getController());
            paths.forEach(path -> {
                logger.info("Enable dynamic route | Service: {} --- {}", path, api.name());
                router.route(path).handler(handler).enable();
            });
        } else {
            paths.forEach(path -> {
                logger.info("Disable dynamic route | Service: {} --- {}", path, api.name());
                router.route(path).disable();
            });
        }
    }

}
