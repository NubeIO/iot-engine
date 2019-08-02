package com.nubeiot.edge.connector.datapoint.service;

import java.util.UUID;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.EntityService.UUIDKeyEntity;
import com.nubeiot.core.sql.JsonTable;
import com.nubeiot.iotdata.model.Tables;
import com.nubeiot.iotdata.model.tables.daos.NetworkDao;
import com.nubeiot.iotdata.model.tables.pojos.Network;
import com.nubeiot.iotdata.model.tables.records.NetworkRecord;

import lombok.NonNull;

public class NetworkService extends DataPointService<UUID, Network, NetworkRecord, NetworkDao>
    implements UUIDKeyEntity<Network, NetworkRecord, NetworkDao> {

    public NetworkService(@NonNull EntityHandler entityHandler, @NonNull HttpClientDelegate client) {
        super(entityHandler, client);
    }

    @Override
    protected String endpoint() {
        return null;
    }

    @Override
    protected @NonNull String listKey() {
        return "points";
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
