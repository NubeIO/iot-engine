package com.nubeiot.edge.connector.bacnet.handlers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.edge.connector.bacnet.BACnetInstance;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;

public class NubeServiceEventHandlerTest {

    static Map<String, BACnetInstance> bacnetInstances = new HashMap<>();
    static LocalDevice localDevice1;
    static LocalDevice localDevice2;
    static NubeServiceEventHandler eventHandler;
    static JsonObject points;

    @BeforeClass
    public static void beforeAll() throws Exception {
        DefaultTransport transport = Mockito.mock(DefaultTransport.class);
        localDevice1 = new LocalDevice(111, transport);
        localDevice2 = new LocalDevice(111, transport);
        Vertx vertx = Mockito.mock(Vertx.class);
        bacnetInstances.put("net1", BACnetInstance.createBACnet(localDevice1, vertx, null));
        bacnetInstances.put("net2", BACnetInstance.createBACnet(localDevice2, vertx, null));
        eventHandler = new NubeServiceEventHandler(bacnetInstances);

        final URL POINTS_RESOURCE = FileUtils.class.getClassLoader().getResource("points.json");
        points = new JsonObject(FileUtils.readFileToString(POINTS_RESOURCE.toString()));
    }

    @Before
    public void beforeEach() throws Exception {
        localDevice1.getLocalObjects().clear();
        localDevice2.getLocalObjects().clear();
        bacnetInstances.forEach((s, baCnetInstance) -> {
            baCnetInstance.addLocalObjectFromJson("UI1", points.getJsonObject("UI1"));
        });
    }

    @Test
    public void objectCreatedTest() throws Exception {
        eventHandler.objectCreated("UI2", points.getJsonObject("UI2"));
        Assert.assertTrue(localDevice1.getObject(new ObjectIdentifier(ObjectType.analogInput, 2)) != null);
        Assert.assertTrue(localDevice2.getObject(new ObjectIdentifier(ObjectType.analogInput, 2)) != null);
    }

    @Test
    public void objectRemovedTest() throws Exception {
        eventHandler.objectRemoved("UI1");
        Assert.assertTrue(localDevice1.getObject(new ObjectIdentifier(ObjectType.analogInput, 1)) == null);
        Assert.assertTrue(localDevice2.getObject(new ObjectIdentifier(ObjectType.analogInput, 1)) == null);
        eventHandler.objectRemoved("BAD_ID");
    }

    @Test
    public void objectUpdatedTest() throws Exception {
        eventHandler.objectUpdated("UI1", "name", "newName");
        Assert.assertEquals(new CharacterString("newName"),
                            localDevice1.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                        .get(PropertyIdentifier.objectName));
        Assert.assertEquals(new CharacterString("newName"),
                            localDevice2.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                        .get(PropertyIdentifier.objectName));

        eventHandler.objectUpdated("UI1", "name", "UI1");
        eventHandler.objectUpdated("UI1", "bad", "newName");
        Assert.assertEquals(new CharacterString("UI1"),
                            localDevice1.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                        .get(PropertyIdentifier.objectName));
        Assert.assertEquals(new CharacterString("UI1"),
                            localDevice2.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                        .get(PropertyIdentifier.objectName));
    }

    @Test
    public void objectWrittenTest() throws Exception {
        float defaultValue = points.getJsonObject("UI1").getFloat("value");
        JsonObject json = new JsonObject();

        eventHandler.objectWritten("UI1", json);
        Assert.assertEquals(new Real(defaultValue),
                            localDevice1.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                        .get(PropertyIdentifier.presentValue));
        Assert.assertEquals(new Real(defaultValue),
                            localDevice2.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                        .get(PropertyIdentifier.presentValue));

        json.put("value", 123);
        eventHandler.objectWritten("UI1", json);
        Assert.assertEquals(new Real(123), localDevice1.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                                       .get(PropertyIdentifier.presentValue));
        Assert.assertEquals(new Real(123), localDevice2.getObject(new ObjectIdentifier(ObjectType.analogInput, 1))
                                                       .get(PropertyIdentifier.presentValue));
    }

}
