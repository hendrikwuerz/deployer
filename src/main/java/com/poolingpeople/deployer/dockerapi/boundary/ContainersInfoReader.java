package com.poolingpeople.deployer.dockerapi.boundary;

import javax.json.*;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
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

        Optional.ofNullable(json.getJsonArray("Ports")).ifPresent(
                jArr -> containerInfo.setPorts(
                        jArr.stream().map(s -> getPort((JsonObject) s))
                                .collect(Collectors.toList()))
        );

        Optional.ofNullable(json.getJsonArray("Names")).ifPresent(
                jArr -> containerInfo.setNames(
                        jArr.stream().map(s -> ((JsonString) s).getString())
                                .collect(Collectors.toList()))
        );

        Optional.ofNullable(json.get("Command")).ifPresent(
                jStr -> containerInfo.setCommand(((JsonString)jStr).getString()));

        Optional.ofNullable(json.get("Image")).ifPresent(
                jStr -> containerInfo.setImage(((JsonString)jStr).getString()));

        Optional.ofNullable(json.get("Status")).ifPresent(
                jStr -> containerInfo.setStatus(((JsonString)jStr).getString()));

        containerInfo
                .setCreated(json.getJsonNumber("Created").longValue())
                .setId(json.getString("Id"));

        return containerInfo;

    }

    private ContainerInfo.Port getPort(JsonObject portObj){

        ContainerInfo.Port port = new ContainerInfo.Port();
        Optional.ofNullable(portObj.get("IP")).ifPresent(jStr -> port.ip = getStringFromJsonValue(jStr));
        Optional.ofNullable(portObj.get("PrivatePort")).ifPresent(jNum -> port.privatePort = getIntFromJsonValue(jNum));
        Optional.ofNullable(portObj.get("Type")).ifPresent(jStr -> port.type = getStringFromJsonValue(jStr));
        Optional.ofNullable(portObj.get("PublicPort")).ifPresent(jNum -> port.publicPort = getIntFromJsonValue(jNum));

        return port;
    }

    private String getStringFromJsonValue(JsonValue value){

        if(value.getValueType() != JsonValue.ValueType.STRING){
            throw new RuntimeException(value + " is not a JsonString value");
        }

        return ((JsonString)value).getString();

    }

    private int getIntFromJsonValue(JsonValue value){

        if(value.getValueType() != JsonValue.ValueType.NUMBER){
            throw new RuntimeException(value + " is not a Number value");
        }

        return ((JsonNumber)value).intValue();

    }

}
