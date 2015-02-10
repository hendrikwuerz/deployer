package com.poolingpeople.deployer.docker.boundary;

import javax.json.*;
import javax.jws.Oneway;

/**
 * Created by alacambra on 09.02.15.
 * "HostConfig": {
 "Binds": ["/tmp:/tmp"],
 "Links": ["redis3:redis"],
 "LxcConf": {"lxc.utsname":"docker"},
 "PortBindings": { "22/tcp": [{ "HostPort": "11022" }] },
 "PublishAllPorts": false,
 "Privileged": false,
 "Dns": ["8.8.8.8"],
 "DnsSearch": [""],
 "ExtraHosts": null,
 "VolumesFrom": ["parent", "other:ro"],
 "CapAdd": ["NET_ADMIN"],
 "CapDrop": ["MKNOD"],
 "RestartPolicy": { "Name": "", "MaximumRetryCount": 0 },
 "NetworkMode": "bridge",
 "Devices": []
 }
 */
public class HostConfigBodyBuilder {

    private CreateContainerBodyBuilder containerBodyBuilder;
    private JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
    private JsonArrayBuilder binds = Json.createArrayBuilder();
    private JsonArrayBuilder links = Json.createArrayBuilder();
    private JsonObjectBuilder lxcConf = Json.createObjectBuilder();
    private JsonObjectBuilder portBindings = Json.createObjectBuilder();
    private boolean publishAllPorts = false;
    private boolean privileged = false;
    private JsonArrayBuilder dns = Json.createArrayBuilder();

    public HostConfigBodyBuilder(CreateContainerBodyBuilder containerBodyBuilder) {
        this.containerBodyBuilder = containerBodyBuilder;
    }

    public CreateContainerBodyBuilder buildHostConfig(){

        objectBuilder.add("Binds", binds);
        objectBuilder.add("Links", links);
        objectBuilder.add("LxcConf", lxcConf);
        objectBuilder.add("PortBindings", portBindings);
        objectBuilder.add("PublishAllPorts", publishAllPorts);
        objectBuilder.add("Privileged", privileged);
        objectBuilder.add("dns", dns);

        containerBodyBuilder.putHostConfigBodyBuilder(objectBuilder.build());

        return containerBodyBuilder;
    }

    public HostConfigBodyBuilder bindTcpPort(String exposedPort, String mappedPort){
        portBindings.add(exposedPort + "/tcp", Json.createArrayBuilder().add(Json.createObjectBuilder().add("HostPort", mappedPort)));
        return this;
    }

    public HostConfigBodyBuilder addLink(String containerName, String alias){
        links.add(containerName + ":" + alias);
        return this;
    }

}
