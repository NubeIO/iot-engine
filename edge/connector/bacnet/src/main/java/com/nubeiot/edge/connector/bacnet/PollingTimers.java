package com.nubeiot.edge.connector.bacnet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import io.vertx.reactivex.core.Vertx;

import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

//TODO: need to set a minimum poll time and maximum timers


public class PollingTimers {

    Vertx vertx;

    public PollingTimers(Vertx vertx) {
        this.vertx = vertx;
    }

    //TODO: change for better memory efficiency
    private Map<String, BACnetInstance> instanceMap = new HashMap<>();
    private Map<Long, Map<String, ObjectIdentifier>> timerGroups = new HashMap<>();

    private void handleTimer(Long timer) {
        Map<String, ObjectIdentifier> objects = timerGroups.get(timer);
        objects.forEach((key, objectIdentifier) -> {
            BACnetInstance bacnetInstance = instanceMap.get(key);
            //TODO: read point
        });
    }

    public void addPoint(BACnetInstance bacnetInstance, RemoteDevice remoteDevice, ObjectIdentifier oid, Long time)
        throws BACnetException {
        boolean newGroupAdded = false;
        String key = getKey(remoteDevice, oid);
        long timerCheck = findTimerGroup(key);
        if (timerCheck != 0) {
            throw new BACnetException("Object already being polled @ " + timerCheck);
        }
        if (!timerGroups.containsKey(time)) {
            timerGroups.put(time, new HashMap<>());
            newGroupAdded = true;
        }
        if (!timerGroups.get(time).containsKey(key)) {
            timerGroups.get(time).put(key, oid);
            instanceMap.put(key, bacnetInstance);
        }
        if (newGroupAdded) {
            vertx.setPeriodic(time, event -> this.handleTimer(event));
        }
    }

    public boolean removePoint(RemoteDevice remoteDevice, ObjectIdentifier oid) throws BACnetException {
        String key = getKey(remoteDevice, oid);
        long timer = findTimerGroup(key);
        if (timer == 0) {
            throw new BACnetException("Object not being polled");
        }

        Map<String, ObjectIdentifier> objectList = timerGroups.get(timer);
        objectList.remove(oid);
        if (objectList.isEmpty()) {
            timerGroups.remove(timer);
            vertx.cancelTimer(timer);
        }
        //need to remove from instanceMap eventually too
        return true;
    }

    private long findTimerGroup(String key) {
        for (Iterator i = timerGroups.entrySet().iterator(); i.hasNext(); ) {
            Entry<Integer, Map<String, ObjectIdentifier>> entry
                = (Entry<Integer, Map<String, ObjectIdentifier>>) i.next();
            if (entry.getValue().containsKey(key)) {
                return entry.getKey();
            }
        }
        return 0;
    }

    private String getKey(RemoteDevice remoteDevice, ObjectIdentifier oid) {
        //TODO: check this can't collide or do more efficient version to store integer instead
        return Integer.toString(remoteDevice.hashCode()) + Integer.toString(oid.hashCode());
    }

}
