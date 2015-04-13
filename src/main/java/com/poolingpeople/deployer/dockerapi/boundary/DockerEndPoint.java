package com.poolingpeople.deployer.dockerapi.boundary;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 2/15/15.
 */
@Named
@SessionScoped
public class DockerEndPoint  implements Serializable{

    String host = "localhost";
    int port = 5555;
    String protocol = "http";

    public DockerEndPoint(String host, int port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    public DockerEndPoint() {
    }

    String accessKey;
    String secretKey;

    // TODO: Same methode like in scenario.boundary.DbSnapshot
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

    // TODO: Same methode like in scenario.boundary.DbSnapshot
    private String streamToString(InputStream in) {
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

    public List<String> getAvailableHosts(){
        loadAWSCredentials();
        AmazonEC2 ec2 = new AmazonEC2Client(new BasicAWSCredentials(accessKey, secretKey));
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DescribeInstancesResult result = ec2.describeInstances();
        List<Instance> instances = result.getReservations().stream().map(res -> (Instance) res.getInstances().get(0)).collect(Collectors.toList());

        return instances.stream()
                .filter(instance -> instance.getTags().stream().filter(tag -> tag.getKey().equals("deployer") && tag.getValue().equals("true"))
                        .count() > 0) // check if there is a tag with deployer==true
                .map(this::getNameForInstance)
                .collect(Collectors.toList());
    }

    private String getNameForInstance(Instance instance) {
        Optional name = instance.getTags().stream()
                .filter(tag -> tag.getKey().equals("Name"))
                .findAny();
        return ((Tag)name.get()).getValue();
    }

    public String getDockerHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getURI(){
        return new StringBuilder(protocol)
                .append("://")
                .append(host)
                .append(":")
                .append(String.valueOf(port))
                .toString();
    }
}
