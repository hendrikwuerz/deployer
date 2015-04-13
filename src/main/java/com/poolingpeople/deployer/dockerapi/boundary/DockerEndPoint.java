package com.poolingpeople.deployer.dockerapi.boundary;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Created by alacambra on 2/15/15.
 */
@Named
@SessionScoped
public class DockerEndPoint  implements Serializable{

    String host = "localhost";
    int port = 5555;
    String protocol = "http";

    public DockerEndPoint(String host, int port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    public DockerEndPoint() {
    }

    public String getDockerHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getURI(){
        return new StringBuilder(protocol)
                .append("://")
                .append(host)
                .append(":")
                .append(String.valueOf(port))
                .toString();
    }
}
