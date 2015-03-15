package com.poolingpeople.deployer.boundary;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.poolingpeople.deployer.dockerapi.boundary.ContainerInfo;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.scenario.boundary.DbSnapshot;
import org.apache.commons.compress.utils.IOUtils;

import javax.inject.Inject;
import java.io.*;

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
    public void makeSnapshot(ContainerInfo containerInfo, String path, String snapshotName){

        String containerId = containerInfo.getId();

        dockerApi.stopContainer(containerId);

        InputStream dbInputStream = dockerApi.copyFiles(containerId, path);
        dbSnapshot.setData(dbInputStream).setBucketName("poolingpeople").setSnapshotName(snapshotName);
        dockerApi.startContainer(containerId);

        dbSnapshot.save();

    }

    public void makeSnapshot(ContainerInfo containerInfo, String snapshotName){

        makeSnapshot(containerInfo, "/var/lib/neo4j/data/graph.db/", snapshotName);

    }
}
