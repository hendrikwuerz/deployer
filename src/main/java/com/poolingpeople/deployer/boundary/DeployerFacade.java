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
import org.apache.commons.compress.utils.IOUtils;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
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

        int maxClusters = 6;

        Set<Integer> result = getActiveContainersNames().stream()//.filter(name -> !name.equals("proxy"))
                .filter(name -> isValidClusterName(name))
                .map(c -> Integer.parseInt(c.split(ClusterConfig.clusterSeparator)[0]))
                .collect(Collectors.toSet());

        for(int i = 1; i < maxClusters; i++){
            if(result.contains(i)) continue;

            return i;
        }

        throw new RuntimeException("No more place for new clusters. Remove some of the existing clusters");
    }

    private boolean isValidClusterName(String name){
        return name.split(ClusterConfig.clusterSeparator).length == 5;
    }

    public void deploy(@NotNull String version, @NotNull String subdomain, String dbSnapshotName, String area, boolean forceDownload){

        clusterConfig
                .setAppBaseName("rest")
                .setAppVersion(version.toLowerCase()) // docker does not accept capitals
                .setServerDomain(endPointProvider.getDockerHost())
                .setConcretDomain(subdomain)
                .setDbScenario(dbSnapshotName)
                .setPortPrefix(String.valueOf(getAvailableCluster()));

        deployNeo4jDb(dbSnapshotName);
        deployWarApplication(version, area, forceDownload);

    }

    /**
     * get the compressed tar file to use in docker for the passed database snapshot name
     * @param dbSnapshotName
     *          The name of the database snapshot to be returned
     * @return
     *          the compressed tar file as a byte array
     */
    public byte[] downloadNeo4J(String dbSnapshotName) {
        clusterConfig.setDbScenario(dbSnapshotName);
        return getTarBytesForNeo4J(dbSnapshotName);
    }

    /**
     * get the compressed tar file to use in docker for the passed version and area
     * @param version
     *          The version to be returned
     * @param area
     *          "snapshots" or "releases"
     * @param forceDownload
     *          whether the use of cached files is allowed or not
     * @return
     *          the compressed tar file as a byte array
     */
    public byte[] downloadWar(String version, String area, boolean forceDownload) {
        clusterConfig
                .setAppBaseName("rest")
                .setAppVersion(version.toLowerCase()) // docker does not accept capitals
                .setServerDomain(endPointProvider.getDockerHost());
        return getTarBytesForWar(version, area, forceDownload);
    }

    /**
     * deploys the neo4j database to docker and starts the server
     * @param dbSnapshotName
     *          The name of the snapshot to be deployed
     */
    private void deployNeo4jDb(String dbSnapshotName){
        CreateContainerBodyWriter builder = null;
        String containerId = null;

        if(dbSnapshotName != null)
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

    /**
     * gets the byte array for the passed version
     *
     * @param dbSnapshotName
     *          The name of the selected db snapshot
     * @return
     *          a byte array with a compressed tar file for docker
     */
    private byte[] getTarBytesForNeo4J(String dbSnapshotName) {
        neo4jDockerPackage.setDbSnapshot(dbSnapshot.setBucketName("poolingpeople").setSnapshotName(dbSnapshotName));
        neo4jDockerPackage.setClusterConfig(clusterConfig);
        neo4jDockerPackage.prepareTarStream();

        return neo4jDockerPackage.getBytes();
    }

    /**
     * deploys the application to docker and starts the server
     * @param version
     *          The version to be deployed
     * @param area
     *          "snapshots" or "releases"
     * @param forceDownload
     *          whether cached files can be used or not
     */
    private void deployWarApplication(String version, String area, boolean forceDownload){

        CreateContainerBodyWriter builder = null;
        String containerId = null;

        byte[] bytes = versionsApi.getWarForVersion(version, area, forceDownload);
        applicationDockerPackage.setClusterConfig(clusterConfig);
        applicationDockerPackage.setWarFileBytes(bytes);
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

    /**
     * gets the byte array for the passed version
     *
     * @param version
     *          The version of the wished war file
     * @param area
     *          "snapshots" or "releases"
     * @param forceDownload
     *          whether cached files can be used or not
     * @return
     *          a byte array with a compressed tar file for docker
     */
    private byte[] getTarBytesForWar(String version, String area, boolean forceDownload) {
        byte[] b = versionsApi.getWarForVersion(version, area, forceDownload);
        applicationDockerPackage.setClusterConfig(clusterConfig);
        applicationDockerPackage.setWarFileBytes(b);
        applicationDockerPackage.prepareTarStream();

        return applicationDockerPackage.getBytes();
    }

    public Collection<String> loadVersions(String area) {
        return versionsApi.loadVersions(area);
    }

    public Collection<String> loadDbSnapshots() {
        return dbSnapshot.getDbSnapshotsList();
    }
}


















