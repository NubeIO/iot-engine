package com.nubeiot.edge.connector.device;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.exceptions.NotFoundException;

public class ConnmanctlNetwork extends NetworkCommand implements UnixCommand {

    private static final String REGEX_WHITE_SPACE = "\\s";
    private final static Logger logger = LoggerFactory.getLogger(ConnmanctlNetwork.class);

    @JsonCreator
    public ConnmanctlNetwork(@JsonProperty(value = "type", required = true) NetworkCommandType type) {
        super(type);
    }

    @Override
    public String configIp(NetworkInfo networkInfo) {
        String service = getService();
        String setIP = String.format("connmanctl config %s --ipv4 manual %s %s %s --nameservers 8.8.8.8 8.8.4.4",
                                     service, networkInfo.getIpAddress(), networkInfo.getSubnetMask(),
                                     networkInfo.getGateway());
        String newIP = execute(setIP);
        logger.info("Set IP: {}", setIP);
        logger.info("New IP: {}", newIP);
        return networkInfo.getIpAddress();
    }

    @Override
    public void configDhcp() {
        String service = getService();
        String setIP = String.format("connmanctl config %s --ipv4 dhcp", service);
        String newIP = execute(setIP);
        logger.info("Set IP: {}", setIP);
        logger.info("New IP: {}", newIP);
    }

    private String getService() {
        String service = execute("connmanctl services");
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
    }

}
