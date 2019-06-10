package com.nubeiot.edge.connector.device.handlers;

import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.device.utils.Command;
import com.nubeiot.edge.connector.device.utils.OS;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class DeviceIpEventHandler implements EventHandler {

    private static final String REGEX_WHITE_SPACE = "\\s";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @EventContractor(action = EventAction.CREATE)
    public JsonObject post(RequestData data) {
        JsonObject address = data.body();
        String ipAddress = address.getString("ipAddress");
        String subnetMask = address.getString("subnetMask");
        String gateway = address.getString("gateway");

        if (OS.isUnix()) {
            String getIp = Command.execute("connmanctl services");
            // Value will be like *AO Wired                ethernet_56000078bbb3_cable
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
                return new JsonObject().put("success", false).put("error", "connmanctl service is not found");
            }
        } else {
            logger.error("Incorrect OS this only works on for conmanctl");
            return new JsonObject().put("success", false).put("error", "Incorrect OS this only works on for conmanctl");
        }
        return new JsonObject().put("success", true).put("message", "Updating to, " + ipAddress + "!");
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.CREATE);
    }

}
