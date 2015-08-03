package com.poolingpeople.deployer.stresstest.boundary;

import com.amazonaws.services.ec2.model.Instance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.poolingpeople.deployer.scenario.boundary.AWSInstances;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hendrik on 20.07.15.
 */
@Named
@SessionScoped
public class StresstestEndPoint implements Serializable {

    private static final String JMETER_MASTER_AWS_TAG = "jmeter-master";
    private static final String JMETER_SERVER_AWS_TAG = "jmeter-server";

    String ip;
    String user = "hendrik";
    String password = "Wuerz";

    String remote = "172.31.37.172,172.31.2.96,172.31.42.20";

    CollectionDataModel<InstanceInfo> awsMaster;
    CollectionDataModel<InstanceInfo> awsServer;
    long lastAWSUpdate;

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
        if(lastAWSUpdate < System.currentTimeMillis() - 30 * 1000) { // update
            updateInstances();
        }
        return awsMaster;
    }

    public CollectionDataModel<InstanceInfo> getAvailableServer() {
        if(lastAWSUpdate < System.currentTimeMillis() - 30 * 1000) { // update
            updateInstances();
        }
        return awsServer;
    }

    private void updateInstances() {
        awsMaster = new CollectionDataModel<>(AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG));
        awsServer = new CollectionDataModel<>(AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG));
        lastAWSUpdate = System.currentTimeMillis();
    }

    public String startMaster() {
        AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG).forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    public String stopMaster() {
        AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG).forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    public String startServer() {
        AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG).forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    public String stopServer() {
        AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG).forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    public String getServerResponse() {
        return serverResponse;
    }


    public void runTest() throws IOException, JSchException {
        // prepare output
        serverResponse = "Starting stresstest";


        // load default values if none are set
        List<Instance> instances = AWSInstances.loadAvailableInstances();

        // insert default ip -> First found JMeter Master
        if(ip == null || ip.equals("")) {
            InstanceInfo master = AWSInstances.findInstance(JMETER_MASTER_AWS_TAG, instances).get(0);
            ip = master.getPrivateIP(); // !!! Only works on AWS not local
        }

        // insert default remotes -> All known JMeter Server
        if(remote == null || remote.equals("")) {
            List<InstanceInfo> server = AWSInstances.findInstance(JMETER_SERVER_AWS_TAG, instances);
            remote = server.stream().map(InstanceInfo::getPrivateIP).collect(Collectors.joining(","));
        }

        serverResponse+= "<br />Master: " + ip;
        serverResponse+= "<br />Server: " + remote;

        // Check instances to be running
        if(!AWSInstances.isIPRunning(ip, instances)) throw new RuntimeException("JMeter Master with IP " + ip + " is not running");
        Arrays.stream(remote.split(",")).forEach( serverIP -> {
            if(!AWSInstances.isIPRunning(serverIP, instances)) throw new RuntimeException("JMeter Server with IP " + serverIP + " is not running");
        });


        // start test
        SSHExecutor ssh = new SSHExecutor(ip, user);
        String command = "cd /home/hendrik/docker-jmeter/hendrik/jmeter-master/; echo " + password + " | sudo -S /home/hendrik/docker-jmeter/hendrik/jmeter-master/example_run_test.sh " + remote + ";";
        System.out.println(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(ssh.execute(command)));

        new Thread() {
            public void run() {

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
        }.start();

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

}
