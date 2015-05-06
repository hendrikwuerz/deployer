package com.poolingpeople.deployer.scenario.boundary;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

import java.util.Optional;

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

    public boolean isStarted() {
        return instance.getState().getName().equals("running");
    }

    public boolean start() {
        return true;
    }

    public boolean stop() {
        return true;
    }

}
