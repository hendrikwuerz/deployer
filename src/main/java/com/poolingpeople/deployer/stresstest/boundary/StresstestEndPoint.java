package com.poolingpeople.deployer.stresstest.boundary;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.poolingpeople.deployer.scenario.boundary.AWSCredentials;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hendrik on 20.07.15.
 */
@Named
@SessionScoped
public class StresstestEndPoint implements Serializable {

    private static final String JMETER_MASTER_AWS_TAG = "jmeter-master";
    private static final String JMETER_SERVER_AWS_TAG = "jmeter-server";
    private static final String BUCKET_NAME = "poolingpeople";
    private static final String RESULT_TAR = "/home/hendrik/jmeter/logs/log.tar";
    private static final String RESULT_TAR_MIN = "/home/hendrik/jmeter/logs/min.tar";

    String ip;
    String user = "hendrik";
    String password = "Wuerz";

    String remote = "172.31.37.172,172.31.2.96,172.31.42.20";
    String plan;

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

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    /**
     * get all instances with AWS Tag jmeter-master
     * uses caching. Only if data is older than 30 seconds a new request will be send
     * @return
     *          All JMeter master instances (normally just one)
     */
    public CollectionDataModel<InstanceInfo> getAvailableMaster() {
        if(lastAWSUpdate < System.currentTimeMillis() - 30 * 1000) { // update
            updateInstances();
        }
        return awsMaster;
    }

    /**
     * get all instances with AWS Tag jmeter-server
     * uses caching. Only if data is older than 30 seconds a new request will be send
     * @return
     *          All JMeter server instances (normally about 3)
     */
    public CollectionDataModel<InstanceInfo> getAvailableServer() {
        if(lastAWSUpdate < System.currentTimeMillis() - 30 * 1000) { // update
            updateInstances();
        }
        return awsServer;
    }

    /**
     * updates the cached data of available JMeter server and master.
     * Sends a request to aws
     */
    private void updateInstances() {
        awsMaster = new CollectionDataModel<>(AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG));
        awsServer = new CollectionDataModel<>(AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG));
        lastAWSUpdate = System.currentTimeMillis();
    }

    /**
     * gets all available test plans on s3 stored in  poolingpeople/stresstest/plans
     * @return
     *          All available test plans
     */
    public Collection<String> getPlans() {

        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());
        List<S3ObjectSummary> objects = s3client.listObjects(BUCKET_NAME).getObjectSummaries();

        if(objects == null) return new ArrayList<>();

        return objects.stream().map(S3ObjectSummary::getKey)
                .filter(o -> { String[] path = o.split("/"); return path.length > 2 && path[0].equals("stresstest") && path[1].equals("plans"); })
                .collect(Collectors.toList());

    }

    /**
     * starts all JMeter master instances on AWS (normally just one)
     */
    public String startMaster() {
        AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG).forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    /**
     * stops all JMeter master instances on AWS (normally just one)
     */
    public String stopMaster() {
        AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG).forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    /**
     * starts all JMeter server instances on AWS (normally about 3)
     */
    public String startServer() {
        AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG).forEach(InstanceInfo::start);
        return "stresstest-control";
    }

    /**
     * stops all JMeter server instances on AWS (normally about 3)
     */
    public String stopServer() {
        AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG).forEach(InstanceInfo::stop);
        return "stresstest-control";
    }

    /**
     * gets the current status of server responses.
     * If test is running the console output of the server can be get with this
     * @return
     *          The output of current (or last) running test
     */
    public String getServerResponse() {
        return serverResponse;
    }


    /**
     * starts a new stress test with the set parameters
     * @throws IOException
     * @throws JSchException
     * @throws SftpException
     *          when test file upload is not possible
     */
    public void runTest() throws IOException, JSchException, SftpException {
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
        serverResponse+= "<br />Plan: " + plan;

        // Check instances to be running
        if(!AWSInstances.isIPRunning(ip, instances)) throw new RuntimeException("JMeter Master with IP " + ip + " is not running");
        Arrays.stream(remote.split(",")).forEach( serverIP -> {
            if(!AWSInstances.isIPRunning(serverIP, instances)) throw new RuntimeException("JMeter Server with IP " + serverIP + " is not running");
        });


        // start test
        SSHExecutor ssh = new SSHExecutor(ip, user);

        // copy selected testplan to JMeter Master
        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());
        S3Object s3Object = s3client.getObject(new GetObjectRequest(BUCKET_NAME, plan));
        InputStream stream = s3Object.getObjectContent();
        ssh.upload("/home/hendrik/jmeter", "neo4jTest.jmx", stream);

        // run the test!
        String command = "cd /home/hendrik/docker-jmeter/hendrik/jmeter-master/; echo " + password + " | sudo -S /home/hendrik/docker-jmeter/hendrik/jmeter-master/example_run_test.sh " + remote + ";";
        System.out.println(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(ssh.execute(command)));

        // handle console output from JMeter Master in another thread
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
                serverResponse += "<br />" + "Copy results to s3";

                try {
                    copyResultsToS3();
                    serverResponse += "<br />" + "Results saved on s3";
                } catch (JSchException | SftpException | IOException e) {
                    e.printStackTrace();
                    serverResponse += "<br />" + "!!!! Results could not be stored on S3 !!!!";
                }

                serverResponse += "<br />" + "Finished Stresstest";
            }
        }.start();

    }

    /**
     * stores the result file on s3
     * @throws JSchException
     * @throws SftpException
     * @throws IOException
     */
    private void copyResultsToS3() throws JSchException, SftpException, IOException {

        // copy result file to tmp file
        SSHExecutor ssh = new SSHExecutor(ip, user);
        File tmpFile = ssh.download(RESULT_TAR);

        // Map file to byte array
        Path path = Paths.get(tmpFile.getAbsolutePath());
        byte[] data = Files.readAllBytes(path);

        // Upload data to s3
        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(data.length);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String filename = dateFormat.format(new Date());
        s3client.putObject(
                new PutObjectRequest(
                        BUCKET_NAME,
                        "stresstest/results/" + filename + ".tar",
                        new ByteArrayInputStream(data),
                        objectMetadata
                ));
    }

    /**
     * download the full result file with all generated data
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     */
    public void getResult() throws SftpException, JSchException, IOException {
        getResultFile(RESULT_TAR);
    }

    /**
     * download the small result file with only minimized data and svg-diagrams
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     */
    public void getResultMin() throws SftpException, JSchException, IOException {
        getResultFile(RESULT_TAR_MIN);
    }

    /**
     * downloads the passed file from the server and let the user save it
     * @param filename
     *          The file on the server to be downloaded
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     */
    private void getResultFile(String filename) throws SftpException, JSchException, IOException {
        SSHExecutor ssh = new SSHExecutor(ip, user);

        File tmpFile = ssh.download(filename);

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
