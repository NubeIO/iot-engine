package com.nubeiot.edge.connector.bacnet.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.cache.ClassGraphCache;
import com.nubeiot.core.cache.LocalCache;
import com.nubeiot.edge.connector.bacnet.mixin.NetworkInterfaceWrapper;

import lombok.NonNull;

public class IpNetworkCache implements LocalCache<String, NetworkInterfaceWrapper> {

    private static final Logger logger = LoggerFactory.getLogger(ClassGraphCache.class);
    private final ConcurrentMap<String, NetworkInterfaceWrapper> cache = new ConcurrentHashMap<>();
    private Function<String, NetworkInterfaceWrapper> discover;

    @Override
    public NetworkInterfaceWrapper get(@NonNull String key) {
        NetworkInterfaceWrapper val = cache.get(key);
        if (Objects.isNull(discover) || Objects.nonNull(val)) {
            logger.info("Get class by {} from cache", key);
            return val;
        }
        logger.info("Find class by {} then put into cache", key);
        return cache.computeIfAbsent(key, discover);
    }

    @Override
    public NetworkInterfaceWrapper remove(@NonNull String key) {
        return cache.get(key);
    }

    @Override
    public Map<String, NetworkInterfaceWrapper> all() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public IpNetworkCache register(Function<String, NetworkInterfaceWrapper> discover) {
        this.discover = discover;
        return this;
    }

}
