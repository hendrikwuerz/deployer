package com.poolingpeople.deployer.dockerapi.boundary;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Created by alacambra on 05.02.15.
 */
public class CreateContainerBodyWriter {

    private JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
    private JsonArrayBuilder cmds = Json.createArrayBuilder();
    private JsonObjectBuilder exposedPortsBuilder = Json.createObjectBuilder();
    private boolean exposedPortsSet = false;
    private boolean cmdsSet = false;
    private boolean hostConfigCreated = false;

    public JsonObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    public CreateContainerBodyWriter setHostName(String value){
        objectBuilder.add("value", value);
        return this;
    }

    public CreateContainerBodyWriter setDomainName(String domainName){
        objectBuilder.add("Domainname", domainName);
        return this;
    }

    public CreateContainerBodyWriter setUser(String user){
        objectBuilder.add("User", user);
        return this;
    }

    public CreateContainerBodyWriter setMemorySwap(String value){
        objectBuilder.add("MemorySwap", value);
        return this;
    }

    public CreateContainerBodyWriter setCpuShares(String value){
        objectBuilder.add("CpuShares", value);
        return this;
    }

    public CreateContainerBodyWriter setCpuset(String value){
        objectBuilder.add("Cpuset", value);
        return this;
    }

    public CreateContainerBodyWriter setAttachStdin(String value){
        objectBuilder.add("AttachStdin", value);
        return this;
    }

    public CreateContainerBodyWriter setAttachStdout(String value){
        objectBuilder.add("AttachStdout", value);
        return this;
    }

    public CreateContainerBodyWriter setAttachStderr(String value){
        objectBuilder.add("AttachStderr", value);
        return this;
    }

    public CreateContainerBodyWriter setTty(String value){
        objectBuilder.add("Tty", value);
        return this;
    }

    public CreateContainerBodyWriter setOpenStdin(String value){
        objectBuilder.add("OpenStdin", value);
        return this;
    }

    public CreateContainerBodyWriter setStdinOnce(String value){
        objectBuilder.add("StdinOnce", value);
        return this;
    }

    public CreateContainerBodyWriter setEnv(String value){
        objectBuilder.add("Env", value);
        return this;
    }

    public CreateContainerBodyWriter setEntrypoint(String value){
        objectBuilder.add("Entrypoint", value);
        return this;
    }

    public CreateContainerBodyWriter setImage(String value){
        objectBuilder.add("Image", value);
        return this;
    }

    public CreateContainerBodyWriter setWorkingDir(String value){
        objectBuilder.add("WorkingDir", value);
        return this;
    }

    public CreateContainerBodyWriter setNetworkDisabled(String value){
        objectBuilder.add("NetworkDisabled", value);
        return this;
    }

    public CreateContainerBodyWriter setMacAddress(String value){
        objectBuilder.add("MacAddress", value);
        return this;
    }

    public CreateContainerBodyWriter exposeTcpPort(int value){

        if(exposedPortsSet)
            throw new RuntimeException("exposed ports already buildHostConfig.");

        exposedPortsBuilder.add(value + "/tcp", Json.createObjectBuilder().build());
        return this;
    }

    public CreateContainerBodyWriter exposeUdpPort(int value){

        if(exposedPortsSet)
            throw new RuntimeException("exposed ports already buildHostConfig.");

        exposedPortsBuilder.add(value + "/udp", Json.createObjectBuilder().build());
        return this;
    }

    public CreateContainerBodyWriter buildExposedPorts(){

        if(exposedPortsSet)
            throw new RuntimeException("exposed ports already buildHostConfig.");

        exposedPortsSet = true;
        objectBuilder.add("ExposedPorts", exposedPortsBuilder);
        return this;
    }

    public CreateContainerBodyWriter setSecurityOpts(String value){
        objectBuilder.add("SecurityOpts", value);
        return this;
    }

    public CreateContainerBodyWriter addCmd(String cmd){

        if(cmdsSet)
            throw new RuntimeException("cmds already buildHostConfig.");


        cmds.add(cmd);
        return this;
    }

    public HostConfigBodyWriter createHostConfig(){

        if(hostConfigCreated)
            throw new RuntimeException("Host config already created");

        hostConfigCreated = true;
        return new HostConfigBodyWriter(this);
    }

    public CreateContainerBodyWriter buildCmds(){

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
