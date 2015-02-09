package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.control.ApplicationDockerPackage;
import com.poolingpeople.deployer.control.Neo4jDockerPackage;
import com.poolingpeople.deployer.control.ProxyDockerPackage;
import com.poolingpeople.deployer.docker.boundary.ContainerCreateBodyBuilder;
import com.poolingpeople.deployer.docker.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;

import javax.inject.Inject;
import java.io.InputStream;
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



    public void deploy(String version, String subdomain){

        logger.info("starting");

        clusterConfig
                .setAppBaseName("rest")
                .setAppVersion(version)
                .setServerDomain("dev.poolingpeople.com")
                .setConcretDomain(subdomain)
                .setNeo4jId("neo")
                .setWildflyId("wf")
                .setPortPrefix("1");


        InputStream is = versionsApi.getWarForVersion(version);
        applicationDockerPackage.setClusterConfig(clusterConfig);
        applicationDockerPackage.setWarFileIS(is);
        applicationDockerPackage.prepareTarStream();

        dockerApi.buildImage("wftest", applicationDockerPackage.getBytes());
//        dockerApi.listImage();
        ContainerCreateBodyBuilder builder = new ContainerCreateBodyBuilder();
        builder.setImage("wftest").addTcpPort(8081);
        String containerId = dockerApi.createContainer(builder);

        logger.finer("Container created:" + containerId);
        dockerApi.startContainer(containerId);

    }
}
