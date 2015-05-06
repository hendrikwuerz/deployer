package com.poolingpeople.deployer.dockerapi.boundary;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.poolingpeople.deployer.scenario.boundary.AWSCredentials;
import com.poolingpeople.deployer.scenario.boundary.InstanceInfo;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.CollectionDataModel;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 2/15/15.
 */
@Named
@SessionScoped
public class DockerEndPoint  implements Serializable{

    String host = "localhost";
    int port = 5555;
    String protocol = "http";
    CollectionDataModel<InstanceInfo> availableHosts;

    public DockerEndPoint(String host, int port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    public DockerEndPoint() {
    }

    public CollectionDataModel<InstanceInfo> getAvailableHosts() {
        availableHosts = new CollectionDataModel<>(DockerEndPoint.loadAvailableHosts());
        return availableHosts;
    }

    public static List<InstanceInfo> loadAvailableHosts(){
        AmazonEC2 ec2 = new AmazonEC2Client(new AWSCredentials());
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DescribeInstancesResult result = ec2.describeInstances();
        List<Instance> instances = result.getReservations().stream().map(res -> (Instance) res.getInstances().get(0)).collect(Collectors.toList());

        return instances.stream()
                .filter(instance -> instance.getTags().stream().filter(tag -> tag.getKey().equals("deployer") && tag.getValue().equals("true"))
                        .count() > 0) // check if there is a tag with deployer==true
                .map(InstanceInfo::new)
                .collect(Collectors.toList());
    }

    public String startInstance() {
        availableHosts.getRowData().start();
        return "change-host";
    }

    public String stopInstance() {
        availableHosts.getRowData().stop();
        return "change-host";
    }

    public String selectInstance() {
        setHost(availableHosts.getRowData().getName());
        return "change-host";
    }

    public String getHost() {
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
