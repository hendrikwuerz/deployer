package com.poolingpeople.deployer.scenario.boundary;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.PostConstruct;
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
    String accessKey;
    String secretKey;
    byte[] data;

    @PostConstruct
    public void init(){
        loadAWSCredentials();
    }

    public void save(){

        if(data == null) throw new RuntimeException("data is not set");

        AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));

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

    public InputStream fetchSnapshot(String snapshotName){

        AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));

        S3Object s3Object = s3client.getObject(
                new GetObjectRequest(
                        bucketName,
                        "neo4j-db/" + snapshotName + ".tar"));

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

    public Collection<String> getDbSnapshotsList(){

        AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
        List<S3ObjectSummary> objects = s3client.listObjects(bucketName).getObjectSummaries();

        if(objects == null) return new ArrayList<>();

        return objects.stream().map(o -> o.getKey())
                .filter(o -> o.contains("neo4j-db") && o.split("/").length > 1)
                .collect(Collectors.toList());

    }

    private void loadAWSCredentials(){

        accessKey = System.getenv("aws-access-key");
        secretKey = System.getenv("aws-secret-key");

        if( accessKey != null && accessKey != null ) return;

        InputStream akStream = getClass().getClassLoader().getResourceAsStream("aws-access-key");
        InputStream skStream = getClass().getClassLoader().getResourceAsStream("aws-secret-key");

        if( akStream == null || skStream == null ){
            throw new RuntimeException("aws keys not found");
        }

        accessKey = streamToString(akStream);
        secretKey = streamToString(skStream);

    }

    private String streamToString(InputStream in)  {

        try {

            StringBuilder out = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                out.append(line);
            }

            br.close();
            return out.toString();

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}