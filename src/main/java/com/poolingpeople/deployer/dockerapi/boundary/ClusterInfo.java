package com.poolingpeople.deployer.dockerapi.boundary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by alacambra on 2/6/15.
 */
public class ClusterInfo {

    Collection<ContainerInfo> containers;
    int clusterNumber;

    public ClusterInfo() {
        containers = new ArrayList<>();
    }

    public Collection<ContainerInfo> getContainers() {
        return containers;
    }

    public void setContainers(Collection<ContainerInfo> containers) {
        this.containers = containers;
    }

    public void addContainer(ContainerInfo container) {
        containers.add(container);
    }

    public int getClusterNumber() {
        return clusterNumber;
    }

    public void setClusterNumber(int clusterNumber) {
        this.clusterNumber = clusterNumber;
    }

    public ContainerInfo getWildfly() {
        return getContainerByServerName("wf");
    }

    public ContainerInfo getNeo4j() {
        return getContainerByServerName("neo4j");
    }

    private ContainerInfo getContainerByServerName(String name) {
        return containers.stream()
                .filter( container -> container.getServer().equals(name) )
                .findAny()
                .get();
    }

    public String getImages() {
        StringBuilder sb = new StringBuilder();
        containers.forEach( container -> sb.append(container.getImage()).append("<br>"));
        return sb.toString();
    }

    public String getLinks() {
        StringBuilder sb = new StringBuilder();
        containers.forEach(container ->
            container.getPorts().stream()
                    .filter( port -> port.ip != null && port.publicPort != null)
                    .forEach( port -> sb
                            .append("<a href='")
                            .append(port.getLink())
                            .append("' target='_blank'>")
                            .append(port.getLink())
                            .append("</a><br>")
            )
        );
        return sb.toString();
    }

    public String getDomainLink() {
        Optional<ContainerInfo> first = containers.stream().findFirst();
        if(first.isPresent()) return "<a href='http://" + first.get().getDomainLink() + "' target='_blank'>" + first.get().getDomainLink() + "</a>";
        return "";
    }

    public String getStatus() {
        if(containers.stream().filter( container -> container.getStatus().startsWith("Up") ).count() == containers.size()) {
            return "Up";
        }
        return "Down";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(clusterNumber).append(System.lineSeparator());
        containers.stream().forEach( container -> sb.append(container).append(System.lineSeparator()));
        return sb.toString();
    }
}
