package com.poolingpeople.deployer.scenario.boundary;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.poolingpeople.deployer.stresstest.boundary.StresstestEndPoint;

import java.util.Arrays;

/**
 * Created by hendrik on 27.04.15.
 */
public class InstanceInfo {

    Instance instance;

    public InstanceInfo(Instance instance) {
        this.instance = instance;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public String getName() {
        return instance.getTags().stream()
                .filter(tag -> tag.getKey().equals("Name"))
                .findAny()
                .get()
                .getValue();
    }

    public String getPrivateIP() {
        return instance.getPrivateIpAddress();
    }

    public String getPublicIP() {
        return instance.getPublicIpAddress();
    }

    public String getStatus() {
        return instance.getState().getName();
    }

    public boolean isStarted() {
        return instance.getState().getName().equals("running");
    }

    public void start() {

        AmazonEC2 ec2 = new AmazonEC2Client(new AWSCredentials());
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));

        StartInstancesRequest startInstancesRequest = new StartInstancesRequest(Arrays.asList(instance.getInstanceId()));
        ec2.startInstances(startInstancesRequest);
    }

    public void stop() {

        AmazonEC2 ec2 = new AmazonEC2Client(new AWSCredentials());
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));

        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(Arrays.asList(instance.getInstanceId()));
        ec2.stopInstances(stopInstancesRequest);
    }

    @Override
    public String toString() {
        return getName() + " - Private IP: " + getPrivateIP() + " - Public IP: " + getPublicIP() + " - Status: " + getStatus();
    }

}
