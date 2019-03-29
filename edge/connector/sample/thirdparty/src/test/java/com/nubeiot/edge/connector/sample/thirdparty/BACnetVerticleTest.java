package com.nubeiot.edge.connector.sample.thirdparty;

import org.mockito.Mockito;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.edge.connector.bacnet.BACnet;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.serotonin.bacnet4j.type.Encodable;

public class BACnetVerticleTest extends BACnetVerticle {

    BACnet bacnetInstance;

    @Override
    public void start(Future<Void> future) {
        super.start();

        bacnetInstance = Mockito.mock(BACnet.class);

        Mockito.when(bacnetInstance.getRemoteDevices()).thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.getRemoteDeviceExtendedInfo(Mockito.anyInt()))
               .thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.getRemoteDeviceObjectList(Mockito.anyInt()))
               .thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.getRemoteObjectProperties(Mockito.anyInt(), Mockito.anyString()))
               .thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.writeAtPriority(Mockito.anyInt(), Mockito.anyString(), Mockito.any(Encodable.class),
                                                    Mockito.anyInt())).thenReturn(Single.just(new JsonObject()));

        registerEventbus(new EventController(vertx));
        addProvider(new MicroserviceProvider(), this::publishServices);

        future.complete();
    }

}
