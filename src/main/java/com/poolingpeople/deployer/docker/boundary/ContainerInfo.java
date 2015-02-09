package com.poolingpeople.deployer.docker.boundary;

import java.util.Collection;

/**
 * Created by alacambra on 2/6/15.
 */
public class ContainerInfo {

    String command;
    Long created;
    String id;
    String image;
    Collection<String> names;
    Collection<Port> ports;
    String status;

    public static class Port {
        String ip;
        Integer privatePort;
        Integer publicPort;
        String type = "tcp";
    }

    public String getCommand() {
        return command;
    }

    public ContainerInfo setCommand(String command) {
        this.command = command;
        return this;
    }

    public Long getCreated() {
        return created;
    }

    public ContainerInfo setCreated(Long created) {
        this.created = created;
        return this;
    }

    public String getId() {
        return id;
    }

    public ContainerInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getImage() {
        return image;
    }

    public ContainerInfo setImage(String image) {
        this.image = image;
        return this;
    }

    public Collection<String> getNames() {
        return names;
    }

    public ContainerInfo setNames(Collection<String> names) {
        this.names = names;
        return this;
    }

    public Collection<Port> getPorts() {
        return ports;
    }

    public ContainerInfo setPorts(Collection<Port> ports) {
        this.ports = ports;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ContainerInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        return command + ":" + created + ":" + id + ":" + image + ":" + names + ":" + ports + ":" + status;
    }
}
