package com.poolingpeople.deployer.docker.boundary;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.Optional;

/**
 * Created by alacambra on 2/10/15.
 */
public class ContainerNetworkSettings {

    private String ipAddress;
    private String bridge;
    private String gateway;

    public ContainerNetworkSettings(JsonObject container){
        JsonObject ns = container.getJsonObject("NetworkSettings");
        Optional.ofNullable(ns.get("IPAddress")).ifPresent(ip -> ipAddress = ((JsonString)ip).getString());
        Optional.ofNullable(ns.get("Gateway")).ifPresent(gw -> gateway = ((JsonString)gw).getString());
        Optional.ofNullable(ns.get("Bridge")).ifPresent(br -> bridge = ((JsonString)br).getString());
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getBridge() {
        return bridge;
    }

    public String getGateway() {
        return gateway;
    }
}
