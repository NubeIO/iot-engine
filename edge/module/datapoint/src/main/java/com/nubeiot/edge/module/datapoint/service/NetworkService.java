package com.nubeiot.edge.module.datapoint.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.HasReferenceEntityService;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.iotdata.edge.model.Tables;
import com.nubeiot.iotdata.edge.model.tables.daos.NetworkDao;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.edge.model.tables.records.NetworkRecord;

import lombok.NonNull;

public final class NetworkService extends AbstractDataPointService<UUID, Network, NetworkRecord, NetworkDao>
    implements UUIDKeyEntity<Network, NetworkRecord, NetworkDao>,
               HasReferenceEntityService<UUID, Network, NetworkRecord, NetworkDao> {

    static String REQUEST_KEY = EntityService.createRequestKeyName(Network.class, Tables.NETWORK.ID.getName());
    private static Set<String> NULL_ALIASES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("default", "gpio")));

    public NetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
    }

    static void optimizeAlias(JsonObject req) {
        Optional.ofNullable(req).ifPresent(r -> {
            if (NetworkService.NULL_ALIASES.contains(r.getString(NetworkService.REQUEST_KEY, "").toLowerCase())) {
                r.put(NetworkService.REQUEST_KEY, (String) null);
            }
        });
    }

    @Override
    public @NonNull String listKey() {
        return "networks";
    }

    @Override
    public @NonNull Class<Network> modelClass() {
        return Network.class;
    }

    @Override
    public @NonNull Class<NetworkDao> daoClass() {
        return NetworkDao.class;
    }

    @Override
    public @NonNull com.nubeiot.iotdata.edge.model.tables.Network table() {
        return Tables.NETWORK;
    }

    @Override
    public Network parse(JsonObject request) {
        return new Network(request);
    }

    @Override
    public Map<String, String> jsonRefFields() {
        return Collections.singletonMap(DeviceService.REQUEST_KEY, table().DEVICE.getName());
    }

    @Override
    public Map<String, Function<String, ?>> jsonFieldConverter() {
        return Collections.singletonMap(DeviceService.REQUEST_KEY, Functions.toUUID());
    }

}
