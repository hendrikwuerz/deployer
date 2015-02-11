package com.poolingpeople.deployer.docker.boundary;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.CollectionDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 09.02.15.
 */

@Named
@RequestScoped
public class ContainerController {

    @Inject
    DockerApi api;

    DataModel<ContainerInfo> containers;

    public DataModel<ContainerInfo> getContainers(){
        Collection<ContainerInfo> containerInfos = api.listContainers();
        containers = new CollectionDataModel<>(containerInfos);
        return containers;
    }

    public String destroy() {
        ContainerInfo current = (ContainerInfo) getContainers().getRowData();
        api.removeContainer(current.getId(), true);
        return "containers-list";
    }
}
