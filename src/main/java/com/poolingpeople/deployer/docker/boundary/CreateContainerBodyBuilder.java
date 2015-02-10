package com.poolingpeople.deployer.docker.boundary;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Created by alacambra on 05.02.15.
 */
public class CreateContainerBodyBuilder {

    private JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
    private JsonArrayBuilder cmds = Json.createArrayBuilder();
    private JsonObjectBuilder exposedPortsBuilder = Json.createObjectBuilder();
    private boolean exposedPortsSet = false;
    private boolean cmdsSet = false;
    private boolean hostConfigCreated = false;

    public JsonObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    public CreateContainerBodyBuilder setHostName(String value){
        objectBuilder.add("value", value);
        return this;
    }

    public CreateContainerBodyBuilder setDomainName(String domainName){
        objectBuilder.add("Domainname", domainName);
        return this;
    }

    public CreateContainerBodyBuilder setUser(String user){
        objectBuilder.add("User", user);
        return this;
    }

    public CreateContainerBodyBuilder setMemorySwap(String value){
        objectBuilder.add("MemorySwap", value);
        return this;
    }

    public CreateContainerBodyBuilder setCpuShares(String value){
        objectBuilder.add("CpuShares", value);
        return this;
    }

    public CreateContainerBodyBuilder setCpuset(String value){
        objectBuilder.add("Cpuset", value);
        return this;
    }

    public CreateContainerBodyBuilder setAttachStdin(String value){
        objectBuilder.add("AttachStdin", value);
        return this;
    }

    public CreateContainerBodyBuilder setAttachStdout(String value){
        objectBuilder.add("AttachStdout", value);
        return this;
    }

    public CreateContainerBodyBuilder setAttachStderr(String value){
        objectBuilder.add("AttachStderr", value);
        return this;
    }

    public CreateContainerBodyBuilder setTty(String value){
        objectBuilder.add("Tty", value);
        return this;
    }

    public CreateContainerBodyBuilder setOpenStdin(String value){
        objectBuilder.add("OpenStdin", value);
        return this;
    }

    public CreateContainerBodyBuilder setStdinOnce(String value){
        objectBuilder.add("StdinOnce", value);
        return this;
    }

    public CreateContainerBodyBuilder setEnv(String value){
        objectBuilder.add("Env", value);
        return this;
    }

    public CreateContainerBodyBuilder setEntrypoint(String value){
        objectBuilder.add("Entrypoint", value);
        return this;
    }

    public CreateContainerBodyBuilder setImage(String value){
        objectBuilder.add("Image", value);
        return this;
    }

    public CreateContainerBodyBuilder setWorkingDir(String value){
        objectBuilder.add("WorkingDir", value);
        return this;
    }

    public CreateContainerBodyBuilder setNetworkDisabled(String value){
        objectBuilder.add("NetworkDisabled", value);
        return this;
    }

    public CreateContainerBodyBuilder setMacAddress(String value){
        objectBuilder.add("MacAddress", value);
        return this;
    }

    public CreateContainerBodyBuilder exposeTcpPort(int value){

        if(exposedPortsSet)
            throw new RuntimeException("exposed ports already buildHostConfig.");

        exposedPortsBuilder.add(value + "/tcp", Json.createObjectBuilder().build());
        return this;
    }

    public CreateContainerBodyBuilder exposeUdpPort(int value){

        if(exposedPortsSet)
            throw new RuntimeException("exposed ports already buildHostConfig.");

        exposedPortsBuilder.add(value + "/udp", Json.createObjectBuilder().build());
        return this;
    }

    public CreateContainerBodyBuilder buildExposedPorts(){

        if(exposedPortsSet)
            throw new RuntimeException("exposed ports already buildHostConfig.");

        exposedPortsSet = true;
        objectBuilder.add("ExposedPorts", exposedPortsBuilder);
        return this;
    }

    public CreateContainerBodyBuilder setSecurityOpts(String value){
        objectBuilder.add("SecurityOpts", value);
        return this;
    }

    public CreateContainerBodyBuilder addCmd(String cmd){

        if(cmdsSet)
            throw new RuntimeException("cmds already buildHostConfig.");


        cmds.add(cmd);
        return this;
    }

    public HostConfigBodyBuilder createHostConfig(){

        if(hostConfigCreated)
            throw new RuntimeException("Host config already created");

        hostConfigCreated = true;
        return new HostConfigBodyBuilder(this);
    }

    public CreateContainerBodyBuilder buildCmds(){

        if(cmdsSet)
            throw new RuntimeException("cmds already buildHostConfig.");

        cmdsSet = true;

        objectBuilder.add("Cmd", cmds);
        return this;
    }

    void putHostConfigBodyBuilder(JsonObject hostConfigObject){
        objectBuilder.add("HostConfig", hostConfigObject);
    }



}
