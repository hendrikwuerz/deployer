package com.poolingpeople.deployer.scenario.boundary;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 04.03.15.
 */
public class DbSnapshot {

    String bucketName = "poolingpeople";
    String snapshotName;
    byte[] data;

    public void save(){

        if(data == null) throw new RuntimeException("data is not set");

        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(data.length);

        s3client.putObject(
                new PutObjectRequest(
                        bucketName,
                        "neo4j-db/" + snapshotName + ".tar",
                        new ByteArrayInputStream(data),
                        objectMetadata
                ));
    }

    public void remove(){

    }

    public void update(){
        throw new RuntimeException("not implemented");
    }

    public InputStream fetchSnapshot(){

        if("".equals(snapshotName) || snapshotName == null){
            throw new RuntimeException("Snapshot name must be given");
        }

        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());

        S3Object s3Object = s3client.getObject(
                new GetObjectRequest(
                        bucketName,
                        "neo4j-db/" + snapshotName));

        InputStream stream = s3Object.getObjectContent();

        return stream;
    }

    private void persistOnFile(String filePath, InputStream inputStream){

        filePath = filePath.charAt(filePath.length() -1) == '/' ? filePath : filePath + "/";

        File f = new File(filePath + snapshotName + ".tar");

        try {

            FileOutputStream fileOutputStream = new FileOutputStream(f);
            IOUtils.copy(inputStream, fileOutputStream);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DbSnapshot setData(InputStream data) {

        try {

            this.data = IOUtils.toByteArray(data);

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

        return this;
    }

    public DbSnapshot setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
        return this;
    }

    public DbSnapshot setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public byte[] getData() {
        return data;
    }

    public Collection<String> getDbSnapshotsList() {
        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("neo4j-db/");

        List<S3ObjectSummary> objects = s3client.listObjects(listObjectsRequest).getObjectSummaries();

        if(objects == null) return new ArrayList<>();

        return objects.stream().map(o -> o.getKey())
                .filter(o -> { String[] path = o.split("/"); return path.length > 1; })
                .collect(Collectors.toList());

    }
}