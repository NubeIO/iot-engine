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

    @EventContractor(action = EventAction.CREATE, returnType = Single.class)
    public Single<JsonObject> post(RequestData data) {
        JsonObject address = data.body();

        String ipAddress = address.getString("ip_address");
        String subnetMask = address.getString("subnet_mask");
        String gateway = address.getString("gateway");

        boolean isInvalid = false;
        List<String> messages = new ArrayList<>();

        if (!Networks.validIP(ipAddress)) {
            isInvalid = true;
            messages.add("IP Address");
        }
        if (!Networks.validIP(subnetMask)) {
            isInvalid = true;
            messages.add("Subnet Mask");
        }
        if (!Networks.validIP(gateway)) {
            isInvalid = true;
            messages.add("Gateway");
        }

        if (isInvalid) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Invalid " + String.join(", ", messages));
        }

        if (OS.isUnix()) {
            String getIp = Command.execute("connmanctl services");
            logger.debug("connmanctl services result:" + getIp);
            // Value will be like: '*AO Wired                ethernet_56000078bbb3_cable'
            if (Strings.isNotBlank(getIp)) {
                getIp = getIp.replaceAll(REGEX_WHITE_SPACE, "");
                getIp = getIp.substring(8); // remove *AO Wired
                String setIP = String.format(
                    "sudo connmanctl config %s --ipv4 manual %s %s %s --nameservers 8.8.8.8 8.8.4.4", getIp, ipAddress,
                    subnetMask, gateway);
                String newIP = Command.execute(setIP);
                logger.info("Set IP: {}", setIP);
                logger.info("New IP: {}", newIP);
            } else {
                throw new NotFoundException("connmanctl service is not found");
            }
        } else {
            throw new NotFoundException("Incorrect OS this only works on for conmanctl");
        }
        return Single.just(
            new JsonObject().put("success", true).put("message", "Updating IP Address to: " + ipAddress));
    }

    @EventContractor(action = EventAction.REMOVE, returnType = Single.class)
    public Single<JsonObject> delete(RequestData data) {
        if (OS.isUnix()) {
            String getIp = Command.execute("connmanctl services");
            logger.debug("connmanctl services result:" + getIp);
            // Value will be like: '*AO Wired                ethernet_56000078bbb3_cable'
            if (getIp != null) {
                getIp = getIp.replaceAll(REGEX_WHITE_SPACE, "");
                getIp = getIp.substring(8); // remove *AO Wired
                logger.debug("IP:" + getIp);
                String setIP = String.format("sudo connmanctl config %s --ipv4 dhcp", getIp);
                String newIP = Command.execute(setIP);
                logger.info("Set IP: {}", setIP);
                logger.info("New IP: {}", newIP);
            } else {
                throw new NotFoundException("connmanctl service is not found");
            }
        } else {
            throw new NotFoundException("Incorrect OS this only works on for conmanctl");
        }
        return Single.just(new JsonObject());
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.unmodifiableList(new ArrayList<>(NetworkEventModels.NETWORK_IP.getEvents()));
    }

}
