package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.dockerapi.boundary.ClusterInfo;
import com.poolingpeople.deployer.dockerapi.boundary.ContainerInfo;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.CollectionDataModel;
import javax.faces.model.DataModel;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by hendrik on 10.03.15.
 */

@Named
@RequestScoped
public class ClusterController {

    @Inject
    DockerApi api;

    DataModel<ClusterInfo> clusters;

    Logger logger = Logger.getLogger(getClass().getName());

    public DataModel<ClusterInfo> getClusters() {

        if(clusters != null)
            return clusters;

        Collection<ClusterInfo> clusterInfos = new ArrayList<>(); // all clusters
        Collection<ContainerInfo> containerInfos = api.listContainers(); // all containers
        containerInfos.stream().sorted( (c1, c2) -> Integer.compare(c2.getCluster(), c1.getCluster()) ).forEach(container -> {
            Optional<ClusterInfo> searchResult = clusterInfos.stream().filter(cluster -> cluster.getClusterNumber() == container.getCluster()).findFirst();
            ClusterInfo cluster; // cluster for the current container
            if (searchResult.isPresent()) { // found clusterNumber for this container
                cluster = searchResult.get();
            } else { // new clusterNumber
                cluster = new ClusterInfo();
                cluster.setClusterNumber(container.getCluster());
                clusterInfos.add(cluster); // add this cluster to the collection
            }
            cluster.addContainer(container);
        });
        clusters = new CollectionDataModel<>(clusterInfos);
        logger.fine(String.valueOf(clusterInfos.size()));
        return clusters;
    }

    public void destroyInvalidClusters() {
        getClusters().forEach( cluster -> {
            if(!cluster.isCorrect()) destroy(cluster);
        });
    }

    public String destroy() {
        return destroy(clusters.getRowData());
    }

    public String destroy(ClusterInfo current) {
        current.getContainers().forEach( container -> api.removeContainer(container.getId(), true) );
        return "clusters-list";
    }

    public String start() {
        logger.fine(String.valueOf(clusters.getRowIndex()));
        ClusterInfo current = clusters.getRowData();
        // neo4j has to be started before wildfly
        try {
            api.startContainer(current.getNeo4j().getId());
            api.startContainer(current.getWildfly().getId());
        } catch (java.util.NoSuchElementException e) {
            current.getContainers().forEach(container -> api.startContainer(container.getId()));
        }
        return "clusters-list";
    }

    public String stop() {
        ClusterInfo current = clusters.getRowData();
        current.getContainers().forEach(container -> api.stopContainer(container.getId()));
        return "clusters-list";
    }
}
