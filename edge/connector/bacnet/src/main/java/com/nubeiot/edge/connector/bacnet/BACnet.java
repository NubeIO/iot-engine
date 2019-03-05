package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.connector.bacnet.Util.LocalPointObjectUtils;
import com.nubeiot.edge.connector.bacnet.Util.NetworkUtils;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.service.Service;
import com.serotonin.bacnet4j.service.confirmed.CreateObjectRequest;
import com.serotonin.bacnet4j.service.confirmed.SubscribeCOVRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.RequestUtils;

/*
 * Main BACnet functionality
 *  - initialisation
 *  - local object adding
 *  - remote device request utils
 */
public class BACnet {

    //TODO: be able to search for single remote device
    //TODO: implement MTSP support


    private LocalDevice localDevice;
    private Vertx vertx;
    private EventController eventController;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BACnet(String name, int id, Future<Void> future, EventController eventController) {

        this.eventController = eventController;

        String broadcastAddress = null;
        int networkPrefixLength = 24;
        try {
            broadcastAddress = NetworkUtils.getBroadcastAddress();
            networkPrefixLength = NetworkUtils.getNetworkPrefixLength();
        } catch (Exception ex) {
            ex.printStackTrace();
            future.fail(ex);
            return;
        }

        IpNetwork network = new IpNetworkBuilder().withBroadcast(broadcastAddress, networkPrefixLength)
                                                  .build();
        Transport transport = new DefaultTransport(network);

        localDevice = new LocalDevice(id, transport);
        localDevice.writePropertyInternal(PropertyIdentifier.modelName, new CharacterString(name));

        try {
            localDevice.initialize();
            localDevice.startRemoteDeviceDiscovery(remoteDevice -> {handleDeviceDiscovered(remoteDevice);});

            if (!localDevice.isInitialized()) {
                throw new BACnetServiceException(ErrorClass.device, ErrorCode.internalError);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            future.fail(ex);
            return;
        }
    }

    private void handleDeviceDiscovered(RemoteDevice remoteDevice) {
        getDeviceObjectList(remoteDevice);
    }

    //    public void BEGIN_TEST() {
    //        Object[] rd = localDevice.getRemoteDeviceCache().getEntities().toArray();
    //        Buffer data = Buffer.buffer(rd.toString());
    //        vertx.fileSystem().writeFile("thingthing.txt", data, res -> {
    //            if(res.succeeded())
    //                System.out.println("Success in writing file from BACnet Connector");
    //            else System.out.println("Failed writing file from BACnet Connector");
    //        });
    //    }

    public void initialiseLocalObjectsFromJson(JsonObject json) {
        json.getMap().forEach((key, obj) -> {
            JsonObject pointJson = JsonObject.mapFrom((LinkedHashMap) obj);
            LocalPointObjectUtils.createLocalObject(pointJson, key, localDevice);
        });
    }

    public void addLocalObjectFromJson(JsonObject json) {
        //TODO: test Json data format that comes through
        //  need point ID somehow as well

        //        LocalPointObjectUtils.createLocalObject(json, id, localDevice);
    }

    //REMOTE DEVICE FUNCTIONS

    public List<RemoteDevice> getRemoteDevices() {
        return localDevice.getRemoteDeviceCache().getEntities();
    }

    public void getDeviceObjectList(RemoteDevice remoteDevice) {
        try {
            SequenceOf<ObjectIdentifier> objectList = RequestUtils.getObjectList(localDevice, remoteDevice);
            remoteDevice.setObjectProperty(remoteDevice.getObjectIdentifier(), PropertyIdentifier.objectList,
                                           objectList);
        } catch (BACnetException e) {
        }
    }


    public void getAllRemoteObjectProperties(RemoteDevice d, ObjectIdentifier oid, Consumer c) throws BACnetException {

        List<ObjectPropertyTypeDefinition> propsDefs = ObjectProperties.getObjectPropertyTypeDefinitions(
            oid.getObjectType());
        ArrayList<PropertyIdentifier> props = new ArrayList<PropertyIdentifier>(propsDefs.size());
        Map<PropertyIdentifier, Encodable> propValuesFinal = new HashMap<>();

        for (ObjectPropertyTypeDefinition prop : propsDefs) {
            props.add(prop.getPropertyTypeDefinition().getPropertyIdentifier());
        }

        Map<PropertyIdentifier, Encodable> propValues = RequestUtils.getProperties(localDevice, d, oid, null,
                                                                                   props.toArray(
                                                                                       new PropertyIdentifier[0]));
        propValues.forEach((pid, val) -> {
            if (val instanceof ErrorClassAndCode) {
                return;
            }
            propValuesFinal.put(pid, val);
            d.setObjectProperty(oid, pid, val);
        });
        c.accept(JsonObject.mapFrom(propValuesFinal));
    }

    public Encodable getPropery(RemoteDevice rd, ObjectIdentifier oid, PropertyIdentifier pid) {
        try {
            if (oid == null) {
                return RequestUtils.getProperty(localDevice, rd, pid);
            } else {
                return RequestUtils.getProperty(localDevice, rd, oid, pid);
            }
        } catch (BACnetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean remoteObjectSubscribeCOV(RemoteDevice rd, ObjectIdentifier obj) {
        boolean correctID = false;
        while (!correctID) {
            try {
                int subID = (int) Math.floor((Math.random() * 100) + 1);
                ;
                UnsignedInteger subProcessID = new UnsignedInteger(subID);
                UnsignedInteger lifetime = new UnsignedInteger(0);
                SubscribeCOVRequest request = new SubscribeCOVRequest(subProcessID, obj, Boolean.TRUE, lifetime);
                localDevice.send(rd, request);
                //TODO: handle property not subscribable -> setup interval polling

                correctID = true;
            } catch (BACnetRuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    public void writeAtPriority(RemoteDevice rd, ObjectIdentifier obj, int val, int priority) {
        WritePropertyRequest req = new WritePropertyRequest(obj, PropertyIdentifier.presentValue, null, new Real(val),
                                                            new UnsignedInteger(priority));
        localDevice.send(rd, req);
    }

    public void writeAtPriorityNull(RemoteDevice rd, ObjectIdentifier obj, int priority) {
        WritePropertyRequest req = new WritePropertyRequest(obj, PropertyIdentifier.presentValue, null, Null.instance,
                                                            new UnsignedInteger(priority));
        localDevice.send(rd, req);
    }

    private class listener extends DeviceEventAdapter {

        @Override
        public void covNotificationReceived(UnsignedInteger subscriberProcessIdentifier,
                                            ObjectIdentifier initiatingDeviceIdentifier,
                                            ObjectIdentifier monitoredObjectIdentifier, UnsignedInteger timeRemaining,
                                            SequenceOf<PropertyValue> listOfValues) {
            String address = "edge.io.cov." + subscriberProcessIdentifier + "." + initiatingDeviceIdentifier + "." +
                             monitoredObjectIdentifier;
            JsonObject json = new JsonObject();
            json.put("subscriberProcessID", subscriberProcessIdentifier);
            json.put("objectID", monitoredObjectIdentifier);
            json.put("value", listOfValues);
            //            vertx.eventBus().publish(address, json);
            //TODO: retrun json to publish
        }

        @Override
        public void requestReceived(Address from, Service service) {
            super.requestReceived(from, service);
            if (service instanceof CreateObjectRequest) {
                CreateObjectRequest req = (CreateObjectRequest) service;
                //TODO: send request to bonescript api for createObjectRequests
                //  resolve why this is a private method
                //  req.getListOfInitialValues();
            }
        }

    }

}
