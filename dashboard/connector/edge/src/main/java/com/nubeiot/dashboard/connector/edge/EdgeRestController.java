package com.nubeiot.dashboard.connector.edge;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.cluster.ClusterNode;
import com.nubeiot.core.cluster.ClusterRegistry;
import com.nubeiot.core.cluster.IClusterDelegate;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.ApiConstants;
import com.zandero.rest.annotation.Get;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@Path(ApiConstants.ROOT_API_PATH)
@Produces(ApiConstants.DEFAULT_CONTENT_TYPE)
public final class EdgeRestController {

    @Get
    @Path("/info")
    public JsonObject info() {
        return new JsonObject().put("name", "edge-connector-rest")
                               .put("version", "1.0.0-SNAPSHOT")
                               .put("vert.x_version", "3.5.4")
                               .put("java_version", "8.0");
    }

    @Get
    @Path("/nodes")
    public List<JsonObject> nodes(@Context Vertx vertx) {
        JsonObject config = vertx.getOrCreateContext().config();
        NubeConfig.SystemConfig.ClusterConfig clusterConfig = IConfig.from(config,
                                                                           NubeConfig.SystemConfig.ClusterConfig.class);
        IClusterDelegate clusterDelegate = ClusterRegistry.instance().getClusterDelegate(clusterConfig.getType());
        return clusterDelegate.getAllNodes().stream().map(ClusterNode::toJson).collect(Collectors.toList());
    }

    @Get
    @Path("/test")
    public JsonObject test() {
        throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Test exception");
    }

}
