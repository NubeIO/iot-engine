package com.nubeiot.edge.connector.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.device.utils.Command;
import com.nubeiot.edge.connector.device.utils.OS;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class NetworkIPEventHandler implements EventHandler {

    private static final String REGEX_WHITE_SPACE = "\\s";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @EventContractor(action = EventAction.UPDATE, returnType = Single.class)
    public Single<JsonObject> update(RequestData data) {
        JsonObject address = data.body();

        String ipAddress = address.getString("ip_address");
        String subnetMask = address.getString("subnet_mask");
        String gateway = address.getString("gateway");

        boolean isInvalid = false;
        List<String> messages = new ArrayList<>();

        if (Strings.isBlank(ipAddress) && !Networks.validIP(ipAddress)) {
            isInvalid = true;
            messages.add("IP Address");
        }
        if (Strings.isBlank(subnetMask) && !Networks.validIP(subnetMask)) {
            isInvalid = true;
            messages.add("Subnet Mask");
        }
        if (Strings.isBlank(gateway) && !Networks.validIP(gateway)) {
            isInvalid = true;
            messages.add("Gateway");
        }

        if (isInvalid) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid " + String.join(", ", messages));
        }

        String service = getService();
        String setIP = String.format("connmanctl config %s --ipv4 manual %s %s %s --nameservers 8.8.8.8 8.8.4.4",
                                     service, ipAddress, subnetMask, gateway);
        String newIP = Command.execute(setIP);
        logger.info("Set IP: {}", setIP);
        logger.info("New IP: {}", newIP);
        return Single.just(new JsonObject().put("success", true).put("message", "Updated IP Address: " + ipAddress));
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData data) {
        String service = getService();
        String setIP = String.format("connmanctl config %s --ipv4 dhcp", service);
        String newIP = Command.execute(setIP);
        logger.info("Set IP: {}", setIP);
        logger.info("New IP: {}", newIP);
        return Single.just(new JsonObject());
    }

    private String getService() {
        if (OS.isUnix()) {
            String service = Command.execute("connmanctl services");
            logger.debug("connmanctl services result:" + service);
            // Value will be like: '*AO Wired                ethernet_56000078bbb3_cable'
            if (service == null) {
                throw new NotFoundException("connmanctl service is not found");
            } else if (service.equals("")) {
                throw new NotFoundException("Ethernet cable may not be plugged-in...");
            }
            service = service.replaceAll(REGEX_WHITE_SPACE, "");
            service = service.substring(8); // remove *AO Wired
            logger.debug("Service: " + service);
            return service;
        } else {
            throw new NotFoundException("Incorrect OS this only works on for conmanctl");
        }
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(NetworkEventModels.NETWORK_IP.getEvents()));
    }

}
