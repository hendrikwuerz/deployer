package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.control.ClusterConfigProvider;
import com.poolingpeople.deployer.control.ProxyDockerPackage;
import com.poolingpeople.deployer.dockerapi.boundary.CreateContainerBodyWriter;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by alacambra on 09.02.15.
 */
public class ConsoleFacade {

    @Inject
    ClusterConfigProvider clusterConfigProvider;

    @Inject
    ProxyDockerPackage proxyDockerPackage;

    @Inject
    DockerApi dockerApi;

    public void createProxy() throws IOException {
        Collection<ClusterConfig> clusterConfigs = clusterConfigProvider.getCurrentClusters("");

        try {
            dockerApi.removeContainer("proxy", true);
        } catch (RuntimeException e){

        }

        proxyDockerPackage.setClusterConfigs(clusterConfigs).prepareTarStream();
        dockerApi.buildImage("proxy", new ByteArrayInputStream(proxyDockerPackage.getBytes()));

        String containerId = null;

        CreateContainerBodyWriter builder = new CreateContainerBodyWriter()
                .setImage("proxy")
                .createHostConfig()
                .bindTcpPort("80", "80")
                .bindTcpPort("7474", "7474")
                .buildHostConfig();

        containerId = dockerApi.createContainer(builder, "proxy");
        dockerApi.startContainer(containerId);
    }
}
