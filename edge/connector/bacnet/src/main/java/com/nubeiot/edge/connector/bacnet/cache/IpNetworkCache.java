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
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.utils.Networks;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IpNetworkCache implements LocalDataCache<String, Ipv4Network> {

    private static final Logger logger = LoggerFactory.getLogger(ClassGraphCache.class);
    private final ConcurrentMap<String, Ipv4Network> cache = new ConcurrentHashMap<>();
    private Function<String, Ipv4Network> discover;

    static IpNetworkCache init() {
        final IpNetworkCache ipNetworkCache = new IpNetworkCache();
        Networks.getActiveInterfacesIPv4()
                .entrySet()
                .stream()
                .map(entry -> Ipv4Network.from(entry.getKey(), entry.getValue()))
                .forEach(ipv4Network -> ipNetworkCache.add(ipv4Network.getName(), ipv4Network));
        return ipNetworkCache.register(s -> Networks.getActiveInterfacesIPv4(s)
                                                    .entrySet()
                                                    .stream()
                                                    .findFirst()
                                                    .map(entry -> Ipv4Network.from(entry.getKey(), entry.getValue()))
                                                    .orElseThrow(() -> new NotFoundException(
                                                        "Not found active network interface with name " + s)));
    }

    @Override
    public Ipv4Network get(@NonNull String key) {
        Ipv4Network val = cache.get(key);
        if (Objects.isNull(discover) || Objects.nonNull(val)) {
            logger.info("Get class by {} from cache", key);
            return val;
        }
        logger.info("Find class by {} then put into cache", key);
        return cache.computeIfAbsent(key, discover);
    }

    @Override
    public Ipv4Network remove(@NonNull String key) {
        return cache.get(key);
    }

    @Override
    public Map<String, Ipv4Network> all() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public IpNetworkCache register(Function<String, Ipv4Network> discover) {
        this.discover = discover;
        return this;
    }

    @Override
    public IpNetworkCache add(@NonNull String key, Ipv4Network ipv4Network) {
        cache.put(key, ipv4Network);
        return this;
    }

}
