package com.nubeiot.edge.connector.device.handlers;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.edge.connector.device.utils.Command;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class DeviceNetworkEventHandler implements EventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        // Network interface
        JsonObject interfaces = getNetworkInterfaces();
        logger.info("Interfaces:" + interfaces);
        // Simple network interface
        List<String> simpleIP = Command.executeWithSplit("ifconfig |  grep inet");
        logger.info("SimpleIp:" + simpleIP);
        // Network arp list of devices on the network
        List<String> networkScan = Command.executeWithSplit("arp -a");
        logger.info("Network Scan:" + networkScan);
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("interfaces", interfaces);
        jsonObject.put("simpleIP", simpleIP);
        jsonObject.put("networkScan", networkScan);
        return jsonObject;
    }

    private JsonObject getNetworkInterfaces() {
        JsonObject networkInterfaces = new JsonObject();

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netInt : Collections.list(nets)) {
                networkInterfaces.put(netInt.getName(), getInetAddress(netInt.getInetAddresses()));
            }
        } catch (SocketException e) {
            logger.warn("Could not get Network Interface", e);
        }

        return networkInterfaces;
    }

    private JsonArray getInetAddress(Enumeration<InetAddress> inetAddresses) {
        JsonArray jsonArray = new JsonArray();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put("address", inetAddress.toString());
            jsonObject.put("hostName", inetAddress.getHostName());
            jsonObject.put("canonicalHostName", inetAddress.getCanonicalHostName());
            jsonObject.put("hostAddress", inetAddress.getHostAddress());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_LIST);
    }

}
