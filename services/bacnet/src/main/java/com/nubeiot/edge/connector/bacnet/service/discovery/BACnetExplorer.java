package com.nubeiot.edge.connector.bacnet.service.discovery;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.iot.connector.discovery.ExplorerServiceApis;
import io.github.zero88.qwe.iot.data.IoTEntities;
import io.reactivex.Single;

import com.nubeiot.edge.connector.bacnet.entity.BACnetEntity;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetService;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;

import lombok.NonNull;

/**
 * Defines public service to expose HTTP API for end-user and/or nube-io service
 */
abstract class BACnetExplorer<K, P extends BACnetEntity<K>, X extends IoTEntities<K, P>> extends AbstractBACnetService
    implements BACnetApis, ExplorerServiceApis<K, P, X> {

    BACnetExplorer(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    @EventContractor(action = "GET_ONE", returnType = Single.class)
    public abstract Single<P> discover(RequestData reqData);

    @Override
    @EventContractor(action = "GET_LIST", returnType = Single.class)
    public abstract Single<X> discoverMany(RequestData reqData);

}
