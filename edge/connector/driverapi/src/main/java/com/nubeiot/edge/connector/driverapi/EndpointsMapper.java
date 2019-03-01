package com.nubeiot.edge.connector.driverapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.connector.driverapi.models.DriverEventModels;

public class EndpointsMapper {


    /* CURRENT DATA STRUCTURE
     *
     * HTTP_ENDPOINT -> EVENT_ACTION -> DRIVER_HANDLER_ADDRESS
     *
     *
     * /Points |------> CREATE |------> ...bacnet.point.create
     *         |               |------> ...modbus.point.create
     *         |
     *         |------> GET_ONE|------> ...bacnet.point.get
     *                         |------> ...modbus.point.get
     *
     *  etc...
     */

    private HashMap<EventModel, HashMap<EventAction, HashMap<String, String>>> endPoints;

    private ArrayList<String> drivers;

    public EndpointsMapper() {

        drivers = new ArrayList<>();
        endPoints = new HashMap<>();
        for (EventModel eventModel : DriverEventModels.getAllHttpModels()) {
            HashMap actionMap = new HashMap<>(eventModel.getEvents().size());
            addEndpoint(eventModel, actionMap);
            endPoints.put(eventModel, actionMap);
        }
    }

    private void addEndpoint(EventModel eventModel, Map actionMap) {
        eventModel.getEvents().iterator().forEachRemaining(eventAction -> {
            actionMap.put(eventAction, new HashMap<>());
        });
    }

    public boolean addEndpointHandler(EventModel eventModel, EventAction eventAction, String driver,
                                      String handlerAddress) {
        driver = driver.toLowerCase();
        endPoints.get(eventModel).get(eventAction).put(driver, handlerAddress);

        if (!drivers.contains(driver)) {
            drivers.add(driver);
        }

        return true;
    }

    public boolean removeEndpointHandler(EventModel eventModel, EventAction eventAction, String driver,
                                         String handlerAddress) {
        endPoints.get(eventModel).get(eventAction).remove(driver);
        return true;
    }

    public boolean driverExists(String driver) {
        return drivers.contains(driver);
    }

    public String getDriverHandler(EventModel eventModel, EventAction eventAction, String driver) {
        if (!driverExists(driver)) {
            return null;
        } else {
            return endPoints.get(eventModel).get(eventAction).get(driver);
        }
    }

}
