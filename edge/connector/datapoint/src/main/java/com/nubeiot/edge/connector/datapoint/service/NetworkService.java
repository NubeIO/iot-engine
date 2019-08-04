package com.nubeiot.edge.connector.datapoint.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.NetworkDao;
import com.nubeiot.iotdata.model.tables.pojos.Network;
import com.nubeiot.iotdata.model.tables.records.NetworkRecord;

import lombok.NonNull;

public final class NetworkService extends AbstractDataPointService<UUID, Network, NetworkRecord, NetworkDao>
    implements UUIDKeyEntity<Network, NetworkRecord, NetworkDao> {

    private static Set<String> NULL_ALIASES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("default", "gpio")));

    static String REQUEST_NETWORK_KEY = EntityService.createRequestKeyName(Network.class, Tables.NETWORK.ID.getName());

    static void optimizeAlias(JsonObject req) {
        Optional.ofNullable(req).ifPresent(r -> {
            if (NetworkService.NULL_ALIASES.contains(r.getString(NetworkService.REQUEST_NETWORK_KEY, null))) {
                r.put(NetworkService.REQUEST_NETWORK_KEY, (String) null);
            }
        });
    }

    public NetworkService(@NonNull EntityHandler entityHandler) {
        super(entityHandler);
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
    public @NonNull JsonTable<NetworkRecord> table() {
        return Tables.NETWORK;
    }

    @Override
    public Network parse(JsonObject request) {
        return new Network(request);
    }

}
