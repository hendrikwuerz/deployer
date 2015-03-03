package com.poolingpeople.deployer.boundary;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import org.apache.commons.compress.utils.IOUtils;

import javax.inject.Inject;
import java.io.*;

/**
 * Created by alacambra on 03.03.15.
 */
public class DbSnapshotManagerFacade {

    @Inject
    DockerApi dockerApi;

    public DbSnapshotManagerFacade() {
    }

    public DbSnapshotManagerFacade(DockerApi dockerApi) {
        this.dockerApi = dockerApi;
    }

    /*
     default for neo4j: "/var/lib/neo4j/data/graph.db/"
     */
    public void makeSnapshot(String containerId, String path, String snapshotName){
        dockerApi.stopContainer(containerId);
        InputStream dbInputStream = dockerApi.copyFiles(containerId, path);
        uploadSnapshot(dbInputStream, snapshotName);
        dockerApi.startContainer(containerId);
    }

    public void uploadSnapshot(InputStream inputStream, String snapshotName){

        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);

            AmazonS3 s3client = new AmazonS3Client(
                    new BasicAWSCredentials("AKIAJDDTB5LUL7KS2ZTA", "nazxF4IuPRHJME+RXZDce1EPQCS05On1ipBhEajg"));

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(bytes.length);
            s3client.putObject(
                    new PutObjectRequest(
                            "poolingpeople",
                            "neo4j-db/" + snapshotName + ".tar",
                            new ByteArrayInputStream(bytes),
                            objectMetadata
                    ));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fetchSnapshot(String snapshotName){
        AmazonS3 s3client = new AmazonS3Client(
                new BasicAWSCredentials("AKIAJDDTB5LUL7KS2ZTA", "nazxF4IuPRHJME+RXZDce1EPQCS05On1ipBhEajg"));

        S3Object s3Object = s3client.getObject(
                new GetObjectRequest(
                        "poolingpeople",
                        "neo4j-db/" + snapshotName + ".tar"));

        InputStream stream = s3Object.getObjectContent();

        File f = new File("/home/alacambra/removeme_" + snapshotName + ".tar");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            IOUtils.copy(stream, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }




}
