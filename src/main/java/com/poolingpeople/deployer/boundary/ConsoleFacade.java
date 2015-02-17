package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.control.ClusterConfigProvider;
import com.poolingpeople.deployer.control.ProxyDockerPackage;
import com.poolingpeople.deployer.docker.boundary.CreateContainerBodyBuilder;
import com.poolingpeople.deployer.docker.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;

import javax.inject.Inject;
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

    public void createProxy(){
        Collection<ClusterConfig> clusterConfigs = clusterConfigProvider.getCurrentClusters("");

        try {
            dockerApi.removeContainer("proxy", true);
        } catch (RuntimeException e){

        }
        proxyDockerPackage.setClusterConfigs(clusterConfigs).prepareTarStream();
        dockerApi.buildImage("proxy", proxyDockerPackage.getBytes());

        String containerId = null;

        CreateContainerBodyBuilder builder = new CreateContainerBodyBuilder()
                .setImage("proxy")
                .createHostConfig()
                .bindTcpPort("80", "80")
                .buildHostConfig();

        containerId = dockerApi.createContainer(builder, "proxy");
        dockerApi.startContainer(containerId);
    }
}
