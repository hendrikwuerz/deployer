package com.poolingpeople.deployer.control;

public class Neo4jDockerPackage extends DockerCluster {

    @Override
    protected DockerCluster addResources() {
        addFile("Dockerfile-neo4j", "Dockerfile");
        return this;
    }

    @Override
    String replaceClusterBars(String original) {
        return original;
    }

}
