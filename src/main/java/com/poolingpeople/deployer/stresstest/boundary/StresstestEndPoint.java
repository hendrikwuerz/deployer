package com.poolingpeople.deployer.stresstest.boundary;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.poolingpeople.deployer.scenario.boundary.AWSCredentials;
import com.poolingpeople.deployer.scenario.boundary.InstanceInfo;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.CollectionDataModel;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hendrik on 20.07.15.
 */
@Named
@SessionScoped
public class StresstestEndPoint implements Serializable {

    public CollectionDataModel<InstanceInfo> getAvailableMaster() {
        return new CollectionDataModel<>(StresstestEndPoint.loadAvailableInstances("jmeter-master"));
    }

    public CollectionDataModel<InstanceInfo> getAvailableServer() {
        return new CollectionDataModel<>(StresstestEndPoint.loadAvailableInstances("jmeter-server"));
    }

    public String startMaster() {
        loadAvailableInstances("jmeter-master").forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    public String stopMaster() {
        loadAvailableInstances("jmeter-master").forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    public String startServer() {
        loadAvailableInstances("jmeter-server").forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    public String stopServer() {
        loadAvailableInstances("jmeter-server").forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    public static List<InstanceInfo> loadAvailableInstances(String key){
        AmazonEC2 ec2 = new AmazonEC2Client(new AWSCredentials());
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DescribeInstancesResult result = ec2.describeInstances();
        List<Instance> instances = result.getReservations().stream().map(res -> (Instance) res.getInstances().get(0)).collect(Collectors.toList());

        return instances.stream()
                .filter(instance -> instance.getTags().stream().filter(tag -> tag.getKey().equals(key) && tag.getValue().equals("true"))
                        .count() > 0) // check if there is a tag with deployer==true
                .map(InstanceInfo::new)
                .collect(Collectors.toList());
    }
}
