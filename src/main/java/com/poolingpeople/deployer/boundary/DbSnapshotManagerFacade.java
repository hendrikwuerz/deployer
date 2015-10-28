package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.dockerapi.boundary.ContainerInfo;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.scenario.boundary.DbSnapshot;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by alacambra on 03.03.15.
 */
public class DbSnapshotManagerFacade {

    @Inject
    DockerApi dockerApi;

    @Inject
    DbSnapshot dbSnapshot;

    public DbSnapshotManagerFacade() {
    }

    public DbSnapshotManagerFacade(DockerApi dockerApi) {
        this.dockerApi = dockerApi;
    }

    /*
     default for neo4j: "/var/lib/neo4j/data/graph.db/"
     */
    public void makeSnapshot(ContainerInfo containerInfo, String path, String snapshotName) throws IOException {

        String containerId = containerInfo.getId();

        dockerApi.stopContainer(containerId);

        InputStream dbInputStream = dockerApi.copyFiles(containerId, path);
        dbSnapshot.setStream(dbInputStream)
                .setBucketName("poolingpeople")
                .setSnapshotName(snapshotName)
                .save();

        dockerApi.startContainer(containerId);

    }

    public void makeSnapshot(ContainerInfo containerInfo, String snapshotName) throws IOException {

        makeSnapshot(containerInfo, "/var/lib/neo4j/data/graph.db/", snapshotName);

    }
}
