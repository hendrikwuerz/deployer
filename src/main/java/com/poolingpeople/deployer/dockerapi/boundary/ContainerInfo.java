package com.poolingpeople.deployer.dockerapi.boundary;

import com.poolingpeople.deployer.entity.ClusterConfig;

import java.util.Collection;
import java.util.Date;

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

        public String getLink() {
            return "http://" + ip + ":" + publicPort;
        }

        @Override
        public String toString() {
            return ip + ":" + publicPort + ":" + privatePort + "/" + type;
        }
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

    public String getShortId() {
        return id.substring(0,10);
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

    public boolean isUp() {
        return getStatus().startsWith("Up");
    }

    public ContainerInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getCluster() {
        try {
            return Integer.parseInt(getImage().split(ClusterConfig.clusterSeparator)[0]);
        } catch (NumberFormatException e) {
            //e.printStackTrace();
            return -1;
        }
    }

    public String getServer() {
        try {
            return getImage().split(ClusterConfig.clusterSeparator)[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    public String getSubdomain() {
        try {
            return getImage().split(ClusterConfig.clusterSeparator)[3];
        } catch (ArrayIndexOutOfBoundsException e) { // proxy has no 'valid' image name -> catch exception
            return "";
        }
    }

    public String getDomainLink() {
        try {
            String[] parts = getImage().split(ClusterConfig.clusterSeparator);
            String host = parts[4].split(":")[0];
            return getSubdomain() + "." + host;
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    @Override
    public String toString() {
        return command + ":" + new Date(created) + ":" + id + ":" + image
                + ":" + names + ":" + ports + ":" + status + " ||";
    }
}
