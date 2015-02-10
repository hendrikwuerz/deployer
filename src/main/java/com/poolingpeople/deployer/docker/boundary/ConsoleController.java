package com.poolingpeople.deployer.docker.boundary;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.DataModel;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 09.02.15.
 */

@Named
@RequestScoped
public class ConsoleController{

    @Inject
    DockerApi api;

    DataModel<ContainerInfo> containers;

    private String selectedContainerId = "";
    private StringBuilder out = new StringBuilder();

    public void setSelectedContainerId(String selectedContainerId) {
        this.selectedContainerId = selectedContainerId;
    }

    public String getSelectedContainerId() {
        return selectedContainerId;
    }

    public String getLogs() {

        if("".equals(selectedContainerId))
            return "no container selected";

        return api.getContainersLogs(selectedContainerId, 50);
    }

    public String getRemoveContainer(){

        if("".equals(selectedContainerId))
            return "no container selected";

        api.removeContainer(selectedContainerId, true);
        return "container removed";

    }

    public String getContainers() {
        return api.listContainers().stream().map(c -> c.toString()).collect(Collectors.joining("\r"));
    }

    public String getImages() {
        return api.listImage();
    }
}
