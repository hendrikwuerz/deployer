package com.poolingpeople.deployer.boundary;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.awt.event.ContainerAdapter;

/**
 * Created by alacambra on 05.02.15.
 */
public class ContainerCreateBodyBuilder {

    JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
    JsonArrayBuilder cmds = Json.createArrayBuilder();
    JsonObjectBuilder exposedPortsBuilder = Json.createObjectBuilder();

    public JsonObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    public ContainerCreateBodyBuilder startObject(){
        objectBuilder.add("Cmd", cmds);
        return this;
    }

    public ContainerCreateBodyBuilder setHostName(String value){
        objectBuilder.add("value", value);
        return this;
    }

    public ContainerCreateBodyBuilder setDomainName(String domainName){
        objectBuilder.add("Domainname", domainName);
        return this;
    }

    public ContainerCreateBodyBuilder setUser(String user){
        objectBuilder.add("User", user);
        return this;
    }

    public ContainerCreateBodyBuilder setMemorySwap(String value){
        objectBuilder.add("MemorySwap", value);
        return this;
    }

    public ContainerCreateBodyBuilder setCpuShares(String value){
        objectBuilder.add("CpuShares", value);
        return this;
    }

    public ContainerCreateBodyBuilder setCpuset(String value){
        objectBuilder.add("Cpuset", value);
        return this;
    }

    public ContainerCreateBodyBuilder setAttachStdin(String value){
        objectBuilder.add("AttachStdin", value);
        return this;
    }

    public ContainerCreateBodyBuilder setAttachStdout(String value){
        objectBuilder.add("AttachStdout", value);
        return this;
    }

    public ContainerCreateBodyBuilder setAttachStderr(String value){
        objectBuilder.add("AttachStderr", value);
        return this;
    }

    public ContainerCreateBodyBuilder setTty(String value){
        objectBuilder.add("Tty", value);
        return this;
    }

    public ContainerCreateBodyBuilder setOpenStdin(String value){
        objectBuilder.add("OpenStdin", value);
        return this;
    }

    public ContainerCreateBodyBuilder setStdinOnce(String value){
        objectBuilder.add("StdinOnce", value);
        return this;
    }

    public ContainerCreateBodyBuilder setEnv(String value){
        objectBuilder.add("Env", value);
        return this;
    }

    public ContainerCreateBodyBuilder setEntrypoint(String value){
        objectBuilder.add("Entrypoint", value);
        return this;
    }

    public ContainerCreateBodyBuilder setImage(String value){
        objectBuilder.add("Image", value);
        return this;
    }

    public ContainerCreateBodyBuilder setWorkingDir(String value){
        objectBuilder.add("WorkingDir", value);
        return this;
    }

    public ContainerCreateBodyBuilder setNetworkDisabled(String value){
        objectBuilder.add("NetworkDisabled", value);
        return this;
    }

    public ContainerCreateBodyBuilder setMacAddress(String value){
        objectBuilder.add("MacAddress", value);
        return this;
    }

    public ContainerCreateBodyBuilder addTcpPort(int value){
        exposedPortsBuilder.add(value + "/tcp", Json.createObjectBuilder().build());
        return this;
    }

    public ContainerCreateBodyBuilder setAddUdpPort(int value){
        exposedPortsBuilder.add(value + "/udp", Json.createObjectBuilder().build());
        return this;
    }

    public ContainerCreateBodyBuilder setSecurityOpts(String value){
        objectBuilder.add("SecurityOpts", value);
        return this;
    }

    public ContainerCreateBodyBuilder addCmd(String cmd){
        cmds.add(cmd);
        return this;
    }


}
