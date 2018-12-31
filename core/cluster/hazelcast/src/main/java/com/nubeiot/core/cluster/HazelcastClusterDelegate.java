package com.nubeiot.core.cluster;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.exceptions.ClusterException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

@ClusterDelegate
public final class HazelcastClusterDelegate implements IClusterDelegate {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastClusterDelegate.class);
    private HazelcastClusterManager manager;

    @Override
    public ClusterType getTypeName() {
        return ClusterType.HAZELCAST;
    }

    private com.hazelcast.config.Config parseConfig(NubeConfig.SystemConfig.ClusterConfig clusterConfig) {
        URL url = FileUtils.toUrl(clusterConfig.getUrl());
        try {
            if (Objects.nonNull(url)) {
                return new XmlConfigBuilder(url).build();
            } else {
                Path path = FileUtils.toPath(clusterConfig.getFile(), "cluster.xml");
                return new XmlConfigBuilder(path.toAbsolutePath().toString()).build();
            }
        } catch (IOException | NubeException | IllegalArgumentException ex) {
            logger.info("Fallback to default", ex);
            return new XmlConfigBuilder().build();
        }
    }

    @Override
    public ClusterManager initClusterManager(NubeConfig.SystemConfig.ClusterConfig clusterConfig) {
        com.hazelcast.config.Config hazelcastCfg = parseConfig(clusterConfig).setProperty("hazelcast.logging.type",
                                                                                          "slf4j");
        String clusterName = clusterConfig.getName();
        if (Strings.isNotBlank(clusterName)) {
            hazelcastCfg.setInstanceName(clusterName);
        }
        this.manager = new HazelcastClusterManager(hazelcastCfg);
        return manager;
    }

    @Override
    public ClusterNode lookupNodeById(String id) {
        final HazelcastInstance hazelcastInstance = this.manager.getHazelcastInstance();
        if (Objects.isNull(hazelcastInstance)) {
            throw new ClusterException("Hazelcast instance was not initialized");
        }
        Member member = hazelcastInstance.getCluster()
                                         .getMembers()
                                         .stream()
                                         .filter(m -> m.getUuid().equals(id))
                                         .findFirst()
                                         .orElseThrow(() -> new NotFoundException("Not found node with id " + id));
        return convert(member);
    }

    @Override
    public List<ClusterNode> getAllNodes() {
        final HazelcastInstance hazelcastInstance = this.manager.getHazelcastInstance();
        if (Objects.isNull(hazelcastInstance)) {
            throw new ClusterException("Hazelcast instance was not initialized");
        }
        return hazelcastInstance.getCluster().getMembers().stream().map(this::convert).collect(Collectors.toList());
    }

    private ClusterNode convert(Member member) {
        //TODO get local address?
        return ClusterNode.builder()
                          .id(member.getUuid())
                          .address(member.getAddress().toString())
                          .localAddress(member.getSocketAddress().toString())
                          .build();
    }

}
