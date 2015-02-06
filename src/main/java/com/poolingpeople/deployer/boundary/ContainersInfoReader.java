package com.poolingpeople.deployer.boundary;

import javax.json.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 2/6/15.
 */
public class ContainersInfoReader {

    public Collection<ContainerInfo> getContainers(InputStream body){

        JsonArray containersObjs = Json.createReader(body).readArray();

        Collection<ContainerInfo> containers =
                containersObjs.stream()
                .map(s -> fromJson((JsonObject) s))
                .collect(Collectors.toList());

        return containers;
    }

    private ContainerInfo fromJson(JsonObject json){

        ContainerInfo containerInfo = new ContainerInfo();

        Collection<ContainerInfo.Port> ports =
                json.getJsonArray("Ports")
                        .stream().map(s -> getPort((JsonObject) s))
                        .collect(Collectors.toList());

        Collection<String> names =
                json.getJsonArray("Names")
                        .stream().map(s -> ((JsonString) s).getString())
                        .collect(Collectors.toList());

        containerInfo
                .setCommand(json.getString("Command"))
                .setCreated(json.getJsonNumber("Created").longValue())
                .setId(json.getString("Id"))
                .setImage(json.getString("Image"))
                .setNames(names)
                .setPorts(ports)
                .setStatus(json.getString("Status"));

        return containerInfo;

    }

    private ContainerInfo.Port getPort(JsonObject portObj){
        ContainerInfo.Port port = new ContainerInfo.Port();
        port.ip = portObj.getString("IP");
        port.privatePort = portObj.getInt("PrivatePort");
        port.publicPort =  portObj.getInt("PublicPort");
        port.type = portObj.getString("Type");

        return port;
    }

}
