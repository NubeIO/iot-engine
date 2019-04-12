package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.vertx.reactivex.core.Vertx;

import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

//TODO: need to set a minimum poll time and maximum timers


public class PollingTimers {

    Vertx vertx;

    private Map<Integer, Map<RemoteDevice, List<ObjectIdentifier>>> timerGroups = new HashMap<>();
    //TODO: fix this for better memory efficiency
    private Map<RemoteDevice, BACnetInstance> instanceMap = new HashMap<>();

    private void handleTimer(int timer) {
        Map<RemoteDevice, List<ObjectIdentifier>> devices = timerGroups.get(timer);
        devices.forEach((remoteDevice, list) -> {
            BACnetInstance bacnetInstance = instanceMap.get(remoteDevice.getInstanceNumber());
            //read point
        });
        //resetTimer
    }

    public void addPoint(BACnetInstance bacnetInstance, RemoteDevice remoteDevice, ObjectIdentifier oid, int time) {
        boolean newGroupAdded = false;
        if (!timerGroups.containsKey(time)) {
            timerGroups.put(time, new HashMap<>());
            newGroupAdded = true;
        }
        if (!timerGroups.get(time).containsKey(remoteDevice)) {
            timerGroups.get(time).put(remoteDevice, new ArrayList<>());
            instanceMap.put(remoteDevice, bacnetInstance);
        }
        timerGroups.get(time).get(remoteDevice).add(oid);

        if (newGroupAdded) {
            //startTimer
        }
    }

    //TODO: need a way to search timerGroups for other RemoteDevices to check if need to remove from instanceMap as well
    // will save memory!
    public boolean removePoint(RemoteDevice remoteDevice, ObjectIdentifier oid) {
        //search
        int timer = findTimerGroup(remoteDevice, oid);
        if (timer == 0) {
            return false;
        }
        //remove
        List<ObjectIdentifier> objectList = timerGroups.get(timer).get(remoteDevice);
        objectList.remove(oid);
        //if list empty remove list and
        if (objectList.isEmpty()) {
            timerGroups.get(timer).remove(remoteDevice);
        }
        //need to remove from instanceMap eventually too
        return true;
    }

    private int findTimerGroup(RemoteDevice remoteDevice, ObjectIdentifier oid) {
        for (Iterator i = timerGroups.entrySet().iterator(); i.hasNext(); ) {
            Entry<Integer, Map<RemoteDevice, List<ObjectIdentifier>>> entry
                = (Entry<Integer, Map<RemoteDevice, List<ObjectIdentifier>>>) i.next();
            if (entry.getValue().containsKey(remoteDevice) && entry.getValue().get(remoteDevice).contains(oid)) {
                return entry.getKey();
            }
        }
        return 0;
    }

}
