package com.poolingpeople.deployer.docker.boundary;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Created by alacambra on 2/15/15.
 */
@SessionScoped
@Named
public class DockerEndPointProvider implements Serializable{
    String ip = "localhost";
    String port = "5555";

    public String getDockerHost() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }
}
