package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.control.ApplicationDockerPackage;
import com.poolingpeople.deployer.control.ClusterConfigProvider;
import com.poolingpeople.deployer.control.Neo4jDockerPackage;
import com.poolingpeople.deployer.control.ProxyDockerPackage;
import com.poolingpeople.deployer.dockerapi.boundary.ContainerNetworkSettingsReader;
import com.poolingpeople.deployer.dockerapi.boundary.CreateContainerBodyWriter;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.dockerapi.boundary.DockerEndPointProvider;
import com.poolingpeople.deployer.entity.ClusterConfig;
import com.poolingpeople.deployer.scenario.boundary.DbSnapshot;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 2/6/15.
 */
public class DeployerFacade implements Serializable {

    @Inject
    VersionsApi versionsApi;

    @Inject
    DockerApi dockerApi;

    @Inject
    Neo4jDockerPackage neo4jDockerPackage;

    @Inject
    ApplicationDockerPackage applicationDockerPackage;

    @Inject
    ProxyDockerPackage proxyDockerPackage;

    @Inject
    ClusterConfig clusterConfig;

    @Inject
    ClusterConfigProvider clusterConfigProvider;

    @Inject
    DockerEndPointProvider endPointProvider;

    @Inject
    DbSnapshot dbSnapshot;


    Logger logger = Logger.getLogger(this.getClass().getName());

    public Collection<String> getActiveContainersNames(){

        Collection<String> l = dockerApi.listContainers().stream().map(
                c -> c.getNames())
                .flatMap(names -> names.stream())
                .filter(n -> n.lastIndexOf("/") == 0)
                .map(n -> n.substring(1, n.length()))
                .collect(Collectors.toList());

        return l;
    }

    public int getAvailableCluster(){

        int maxClusters = 8;

        Set<Integer> result = getActiveContainersNames().stream()//.filter(name -> !name.equals("proxy"))
                .filter(name -> isValidClusterName(name))
                .map(c -> Integer.parseInt(c.split(ClusterConfig.clusterSeparator)[0]))
                .collect(Collectors.toSet());

        for(int i = 1; i < maxClusters; i++){
            if(result.contains(i)) continue;

            return i;
        }

        throw new RuntimeException("No more place for new clusters");
    }

    private boolean isValidClusterName(String name){
        return name.split(ClusterConfig.clusterSeparator).length == 5;
    }

    public void deploy(@NotNull String version, @NotNull String subdomain, String dbSnapshotName, String area){

        clusterConfig
                .setAppBaseName("rest")
                .setAppVersion(version)
                .setServerDomain(endPointProvider.getDockerHost())
                .setConcretDomain(subdomain)
                .setDbScenario(dbSnapshotName)
                .setPortPrefix(String.valueOf(getAvailableCluster()));

        deployNeo4jDb(dbSnapshotName);
        deployWarApplication(version, area);

    }

    private void deployNeo4jDb(String dbSnapshotName){
        CreateContainerBodyWriter builder = null;
        String containerId = null;

        neo4jDockerPackage.setDbSnapshot(dbSnapshot.setBucketName("poolingpeople").setSnapshotName(dbSnapshotName));
        neo4jDockerPackage.setClusterConfig(clusterConfig);
        neo4jDockerPackage.prepareTarStream();
        dockerApi.buildImage(clusterConfig.getNeo4jId(), neo4jDockerPackage.getBytes());

        builder = new CreateContainerBodyWriter();
        builder.setImage(clusterConfig.getNeo4jId())
                .buildExposedPorts()
                .createHostConfig()
                .bindTcpPort(clusterConfig.getNeo4jPort(), clusterConfig.getPortPrefix() + clusterConfig.getNeo4jPort())
                .buildHostConfig();

        containerId = dockerApi.createContainer(builder, clusterConfig.getNeo4jId());
        dockerApi.startContainer(containerId);

        ContainerNetworkSettingsReader networkSettings = dockerApi.getContainerNetwotkSettings(containerId);
        clusterConfig.setGateway(networkSettings.getGateway());
    }

    private void deployWarApplication(String version, String area){

        CreateContainerBodyWriter builder = null;
        String containerId = null;

        InputStream is = versionsApi.getWarForVersion(version, area);
        applicationDockerPackage.setClusterConfig(clusterConfig);
        applicationDockerPackage.setWarFileIS(is);
        applicationDockerPackage.prepareTarStream();

        dockerApi.buildImage(clusterConfig.getWildflyId(), applicationDockerPackage.getBytes());

        builder = new CreateContainerBodyWriter()
                .setImage(clusterConfig.getWildflyId())
                .createHostConfig()
                .bindTcpPort(clusterConfig.getWfPort(), clusterConfig.getPortPrefix() + clusterConfig.getWfPort())
                .bindTcpPort(clusterConfig.getWfAdminPort(), clusterConfig.getPortPrefix() + clusterConfig.getWfAdminPort())
                .addLink(clusterConfig.getNeo4jId(), clusterConfig.getNeo4jId())
                .buildHostConfig();

        containerId = dockerApi.createContainer(builder, clusterConfig.getWildflyId());

        logger.finer("Container created:" + containerId);
        dockerApi.startContainer(containerId);
    }

    public Collection<String> loadVersions(String area) {
        return versionsApi.loadVersions(area);
    }

    public Collection<String> loadDbSnapshots() {
        return dbSnapshot.getDbSnapshotsList();
    }
}


















