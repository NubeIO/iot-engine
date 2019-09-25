package com.nubeiot.edge.connector.bacnet.handlers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bacnet.BACnetConfig;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.nubeiot.edge.connector.bacnet.objectModels.EdgePoint;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

@RunWith(MockitoJUnitRunner.class)
public class BACnetEventListenerTest {

    protected final HashMap<String, BACnetInstance> bacnetInstances = new HashMap<>();
    private LocalDevice localDevice1;
    private LocalDevice localDevice2;
    BACnetEventListener eventListener;
    String pointId = "UO1";
    private JsonObject points;
    private JsonObject writablePoint;

    @Mock
    ServiceDiscoveryController localController;

    @Before
    public void before() throws Exception {
        Vertx vertx = Mockito.mock(Vertx.class);
        Transport transport = Mockito.mock(DefaultTransport.class);
        localDevice1 = new LocalDevice(111, transport);
        localDevice2 = new LocalDevice(222, transport);
        BACnetInstance mainInstance = BACnetInstance.createBACnet(localDevice1, vertx);
        bacnetInstances.put("ip1", mainInstance);
        bacnetInstances.put("ip2", BACnetInstance.createBACnet(localDevice2, vertx));
        eventListener = new BACnetEventListener(new BACnetConfig(), mainInstance, localDevice1, null, localController,
                                                bacnetInstances, vertx);

        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));
    }

    @After
    public void afterEach() throws Exception {
        localDevice1.terminate();
        localDevice2.terminate();
    }

    private void initPointsFromJson() throws Exception {
        writablePoint = points.getJsonObject(pointId);
        if (writablePoint == null) {
            throw new NullPointerException("Point doesn't exist in json file");
        }
        for (Entry<String, BACnetInstance> e : bacnetInstances.entrySet()) {
            e.getValue().addLocalObject(EdgePoint.fromJson(pointId, writablePoint));
        }
    }

    @Test
    public void handleWriteRequest() throws Exception {
        initPointsFromJson();
        ObjectIdentifier oid = BACnetDataConversions.getObjectIdentifierFromNube(pointId);
        Assert.assertNotNull(localDevice1.getObject(oid));
        Assert.assertNotNull(localDevice2.getObject(oid));
        float newVal = ((Real) localDevice1.getObject(oid).get(PropertyIdentifier.presentValue)).floatValue() + 10f;
        WritePropertyRequest req = new WritePropertyRequest(oid, PropertyIdentifier.presentValue, null,
                                                            new Real(newVal), new UnsignedInteger(1));

        Address from = Mockito.mock(Address.class);

        Mockito.when(
            localController.executeHttpService(Mockito.anyObject(), Mockito.anyString(), Mockito.any(HttpMethod.class),
                                               Mockito.any(RequestData.class)))
               .thenReturn(Single.just(ResponseData.noContent()));

        Assert.assertEquals(localDevice1.getObject(oid).get(PropertyIdentifier.presentValue),
                            localDevice2.getObject(oid).get(PropertyIdentifier.presentValue));
        eventListener.requestReceived(from, req);
        Assert.assertEquals(new Real(newVal), localDevice1.getObject(oid).get(PropertyIdentifier.presentValue));
        Assert.assertEquals(new Real(newVal), localDevice2.getObject(oid).get(PropertyIdentifier.presentValue));
    }

    @Test
    public void handleWriteRequestNoObject() throws Exception {
        WritePropertyRequest req = new WritePropertyRequest(new ObjectIdentifier(ObjectType.analogValue, 1),
                                                            PropertyIdentifier.presentValue, null, new Real(1),
                                                            new UnsignedInteger(1));

        Address from = Mockito.mock(Address.class);
    }

    @Test
    public void handleWriteRequestBadProperty() throws Exception {
        WritePropertyRequest req = new WritePropertyRequest(new ObjectIdentifier(ObjectType.analogValue, 1),
                                                            PropertyIdentifier.objectName, null, new Real(1),
                                                            new UnsignedInteger(1));

        Address from = Mockito.mock(Address.class);
    }

}
