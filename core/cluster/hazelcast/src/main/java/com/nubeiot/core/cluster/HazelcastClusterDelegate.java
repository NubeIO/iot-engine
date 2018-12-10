package com.nubeiot.core.cluster;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigBuilder;
import com.hazelcast.config.XmlConfigBuilder;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.FileUtils;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

@ClusterDelegate
public final class HazelcastClusterDelegate implements IClusterDelegate {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastClusterDelegate.class);

    @Override
    public String getTypeName() {
        return "hazelcast";
    }

    private ConfigBuilder parseClusterConfig(JsonObject clusterOption) {
        URL url = FileUtils.toUrl(clusterOption.getString("url", null));
        try {
            if (Objects.nonNull(url)) {
                return new XmlConfigBuilder(url);
            } else {
                Path path = FileUtils.toPath(clusterOption.getString("file"), "cluster.xml");
                return new XmlConfigBuilder(path.toAbsolutePath().toString());
            }
        } catch (IOException | NubeException | IllegalArgumentException ex) {
            logger.info("Fallback to default", ex);
            return new XmlConfigBuilder();
        }
    }

    @Override
    public ClusterManager initClusterManager(JsonObject clusterConfig) {
        logger.info("Cluster Configuration: {}", clusterConfig);
        Config hazelcastCfg = parseClusterConfig(clusterConfig).build().setProperty("hazelcast.logging.type", "slf4j");
        return new HazelcastClusterManager(hazelcastCfg);
    }

}
