package com.nubeiot.edge.connector.bacnet;

import org.mockito.Mockito;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.serotonin.bacnet4j.type.Encodable;

public class MockBACnetVerticle extends BACnetVerticle {

    @Override
    protected void startBACnet(BACnetConfig baCnetConfig) {
        BACnetInstance bacnetInstance = Mockito.mock(BACnetInstance.class);

        Mockito.when(bacnetInstance.getRemoteDevices()).thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.getRemoteDeviceExtendedInfo(Mockito.anyInt()))
               .thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.getRemoteDeviceObjectList(Mockito.anyInt()))
               .thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.getRemoteObjectProperties(Mockito.anyInt(), Mockito.anyString()))
               .thenReturn(Single.just(new JsonObject()));
        Mockito.when(bacnetInstance.writeAtPriority(Mockito.anyInt(), Mockito.anyString(), Mockito.any(Encodable.class),
                                                    Mockito.anyInt())).thenReturn(Single.just(new JsonObject()));

        this.registerEventbus(new EventController(vertx));
        this.addProvider(new MicroserviceProvider(), this::publishServices);
    }

    @Override
    protected void publishServices(MicroContext microContext) {
        System.out.println("PUBLISHING URLS...");
        microContext.getLocalController()
                    .addEventMessageRecord("bacnet-device-service", BACnetEventModels.DEVICES.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/devices",
                                                                               "/bacnet/devices/:deviceID"),
                                           new JsonObject())
                    .subscribe();

        microContext.getLocalController()
                    .addEventMessageRecord("bacnet-points-service", BACnetEventModels.POINTS.getAddress(),
                                           EventMethodDefinition.createDefault("/bacnet/devices/:deviceID/points",
                                                                               "/bacnet/devices/:deviceID/points" +
                                                                               "/:objectID"), new JsonObject())
                    .subscribe();
    }

    @Override
    public String configFile() { return "bacnet.json"; }

}
