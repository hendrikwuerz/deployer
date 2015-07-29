package com.poolingpeople.deployer.stresstest.boundary;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.poolingpeople.deployer.scenario.boundary.AWSCredentials;
import com.poolingpeople.deployer.scenario.boundary.InstanceInfo;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.CollectionDataModel;
import javax.inject.Named;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hendrik on 20.07.15.
 */
@Named
@SessionScoped
public class StresstestEndPoint implements Serializable {

    String ip = "52.18.254.52";
    String user = "hendrik";
    String password = "Wuerz";

    String remote = "172.31.37.172,172.31.2.96,172.31.42.20";

    String serverResponse;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }


    public CollectionDataModel<InstanceInfo> getAvailableMaster() {
        return new CollectionDataModel<>(StresstestEndPoint.loadAvailableInstances("jmeter-master"));
    }

    public CollectionDataModel<InstanceInfo> getAvailableServer() {
        return new CollectionDataModel<>(StresstestEndPoint.loadAvailableInstances("jmeter-server"));
    }

    public String startMaster() {
        loadAvailableInstances("jmeter-master").forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    public String stopMaster() {
        loadAvailableInstances("jmeter-master").forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    public String startServer() {
        loadAvailableInstances("jmeter-server").forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    public String stopServer() {
        loadAvailableInstances("jmeter-server").forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    public String getServerResponse() {
        return serverResponse;
    }


    public void runTest() throws IOException, JSchException {
        // prepare output
        serverResponse = "Starting stresstest";

        SSHExecutor ssh = new SSHExecutor(ip, user);
        String command = "cd /home/hendrik/docker-jmeter/hendrik/jmeter-master/; echo " + password + " | sudo -S /home/hendrik/docker-jmeter/hendrik/jmeter-master/example_run_test.sh " + remote + ";";
        System.out.println(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(ssh.execute(command)));

        String msg = null;
        try {
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
                serverResponse += "<br />" + msg;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close connections and remove tmp files
        ssh.clean();
        serverResponse += "<br />" + "Finished Stresstest";

    }

    public void getResult() throws SftpException, JSchException, IOException {
        SSHExecutor ssh = new SSHExecutor(ip, user);

        File tmpFile = ssh.scp("/home/hendrik/jmeter/logs/log.tar");

        // parse tmp file to byte array to return it to client
        Path path = Paths.get(tmpFile.getAbsolutePath());
        byte[] data = Files.readAllBytes(path);
        tmpFile.delete();

        // return file
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseHeader("Content-Type", "application/tar");
        externalContext.setResponseHeader("Content-Length", String.valueOf(data.length));
        externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"log.tar\"");
        try {
            externalContext.getResponseOutputStream().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        facesContext.responseComplete();
    }

    public static List<InstanceInfo> loadAvailableInstances(String key){
        AmazonEC2 ec2 = new AmazonEC2Client(new AWSCredentials());
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DescribeInstancesResult result = ec2.describeInstances();
        List<Instance> instances = result.getReservations().stream().map(res -> (Instance) res.getInstances().get(0)).collect(Collectors.toList());

        return instances.stream()
                .filter(instance -> instance.getTags().stream().filter(tag -> tag.getKey().equals(key) && tag.getValue().equals("true"))
                        .count() > 0) // check if there is a tag with deployer==true
                .map(InstanceInfo::new)
                .collect(Collectors.toList());
    }
}
