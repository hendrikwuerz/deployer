package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 2/6/15.
 */
public class ClusterConfigProvider {

    @Inject
    DockerApi dockerApi;

    public Collection<ClusterConfig> getCurrentClusters(String domain){

        Collection<List<ClusterConfig>> cfgs =
                getActiveContainerNames().stream().filter(ClusterConfig::isLoadableContainerName) // only get pp container
                        .map(cf -> new ClusterConfig().loadFromContainerName(cf))
                        .collect(Collectors.groupingBy(ClusterConfig::getId))
                        .values();

        List<ClusterConfig> clusterConfigs =
                cfgs.stream().map(s -> s.stream().reduce(new ClusterConfig(), (a, b) -> merge(a, b)))
                .collect(Collectors.toList());

        return clusterConfigs;

    }

    private ClusterConfig merge(ClusterConfig a, ClusterConfig b){
        b.mergeWith(a);
        return b;
    }

    private Collection<String> getActiveContainerNames(){

        Collection<String> l =
                dockerApi.listContainers()
                        .stream().map(c -> c.getNames())
                .flatMap(names -> names.stream())
                .filter(n -> n.lastIndexOf("/") == 0)
                .map(n -> n.substring(1, n.length()))
                .collect(Collectors.toList());

        return l;
    }
}
