package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.dockerapi.boundary.ContainerInfo;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.dockerapi.boundary.DockerEndPoint;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 09.02.15.
 */

@Named
@RequestScoped
public class ConsoleController{

    @Inject
    DockerApi api;

    @Inject
    DockerEndPoint endPoint;

    @Inject
    ConsoleFacade facade;

    @Inject
    Event<DockerEndPoint> dockerEndPointChangeEvent;

    DataModel<ContainerInfo> containers;

    private String selectedContainerId = "";
    private StringBuilder out = new StringBuilder();

    public String openLog(String selectedContainerId) {

        setSelectedContainerId(selectedContainerId);

        return "/console/container-logs";
    }

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

    public void reloadProxy() throws IOException {
        facade.createProxy();
    }


//    public String destroy() {
//        current = (Category) getItems().getRowData();
//        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
//        performDestroy();
//        recreatePagination();
//        recreateModel();
//        return "List";
//    }
}
