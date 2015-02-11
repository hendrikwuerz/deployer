package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.control.ApplicationDockerPackage;
import com.poolingpeople.deployer.control.Neo4jDockerPackage;
import com.poolingpeople.deployer.control.ProxyDockerPackage;
import com.poolingpeople.deployer.docker.boundary.ContainerNetworkSettings;
import com.poolingpeople.deployer.docker.boundary.CreateContainerBodyBuilder;
import com.poolingpeople.deployer.docker.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by alacambra on 2/6/15.
 */
public class DeployerFacade {

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

    Logger logger = Logger.getLogger(this.getClass().getName());



    public void deploy(@NotNull String version, @NotNull String subdomain, @NotNull String imageName){

        logger.info("starting");

        String neoName = "neo4j" + new Date().getTime();

        clusterConfig
                .setAppBaseName("rest")
                .setAppVersion(version)
                .setServerDomain("dev.poolingpeople.com")
                .setConcretDomain(subdomain)
                .setNeo4jId(neoName)
                .setWildflyId("wf")
                .setPortPrefix("1");

        CreateContainerBodyBuilder builder = null;
        String containerId = null;

        neo4jDockerPackage.setClusterConfig(clusterConfig);
        neo4jDockerPackage.prepareTarStream();
        dockerApi.buildImage("neo4j", neo4jDockerPackage.getBytes());
        builder = new CreateContainerBodyBuilder();
        builder.setImage("neo4j").buildExposedPorts().createHostConfig().bindTcpPort("7474", "17474").buildHostConfig();

        containerId = dockerApi.createContainer(builder, neoName);
        dockerApi.startContainer(containerId);

        ContainerNetworkSettings settings = dockerApi.getContainerNetwotkSettings(containerId);
        System.out.println("-------------------" + settings.getGateway());



        InputStream is = versionsApi.getWarForVersion(version);
        applicationDockerPackage.setClusterConfig(clusterConfig);
        applicationDockerPackage.setWarFileIS(is);
        applicationDockerPackage.prepareTarStream();
        applicationDockerPackage.materializeTarFile("/home/alacambra/file.tar.gz");


        dockerApi.buildImage(imageName, applicationDockerPackage.getBytes());
        builder = new CreateContainerBodyBuilder();
        builder
                .setImage(imageName)
                .buildExposedPorts()
                .createHostConfig()
                .bindTcpPort(clusterConfig.getWfPort(), clusterConfig.getPortPrefix() + clusterConfig.getWfPort())
                .bindTcpPort(clusterConfig.getWfAdminPort(), clusterConfig.getPortPrefix() + clusterConfig.getWfAdminPort())
                .addLink(neoName, neoName)
                .buildHostConfig();
        containerId = dockerApi.createContainer(builder, imageName + new Date().getTime());

        logger.info("Container created:" + containerId);
        dockerApi.startContainer(containerId);

    }
}
