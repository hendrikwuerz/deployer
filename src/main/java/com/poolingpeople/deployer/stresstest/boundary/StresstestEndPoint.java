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
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.CollectionDataModel;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
@ApplicationScoped
public class StresstestEndPoint implements Serializable {

    public static final String JMETER_MASTER_AWS_TAG = "jmeter-master";
    public static final String JMETER_SERVER_AWS_TAG = "jmeter-server";
    private static final String BUCKET_NAME = "poolingpeople";
    private static final String RESULT_TAR = "/jmeter/logs/log.tar";
    private static final String RESULT_TAR_MIN = "/jmeter/logs/min.tar";
    private static final String RESULT_JTL = "/jmeter/logs/jtl.jtl";

    String ip;
    String user = "jmeter";

    String jmeterHome; // filled with env variable from jmeter master
    String remote = "";
    String plan;
    String finalTestPlan;

    private String testPlanIp;
    private String testPlanPort;
    private String testPlanThreads;
    private String testPlanLoops;

    Collection<String> plans;
    long lastPlanUpdate;

    CollectionDataModel<InstanceInfo> awsMaster;
    CollectionDataModel<InstanceInfo> awsServer;
    long lastAWSUpdate;

    String serverResponse;
    private boolean testIsRunning = false;

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

    public String getTestPlanIp() {
        return testPlanIp;
    }

    public void setTestPlanIp(String testPlanIp) {
        this.testPlanIp = testPlanIp;
    }

    public String getTestPlanPort() {
        return testPlanPort;
    }

    public void setTestPlanPort(String testPlanPort) {
        this.testPlanPort = testPlanPort;
    }

    public String getTestPlanThreads() {
        return testPlanThreads;
    }

    public void setTestPlanThreads(String testPlanThreads) {
        this.testPlanThreads = testPlanThreads;
    }

    public String getTestPlanLoops() {
        return testPlanLoops;
    }

    public void setTestPlanLoops(String testPlanLoops) {
        this.testPlanLoops = testPlanLoops;
    }

    public boolean isTestRunning() {
        return testIsRunning;
    }

    public void setTestIsRunning(boolean running) {
        testIsRunning = running;
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
    public void updateInstances() {
        awsMaster = new CollectionDataModel<>(AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG));
        awsServer = new CollectionDataModel<>(AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG));
        lastAWSUpdate = System.currentTimeMillis();
    }

    /**
     * returns the testplans. If cached date is older than 60 sec it becomes updated
     * @return
     *          The available test plans
     */
    public Collection<String> getPlans() {
        if(lastPlanUpdate < System.currentTimeMillis() - 60 * 1000) { // update
            updatePlans();
        }
        return plans;
    }

    /**
     * gets all available test plans on s3 stored in  poolingpeople/stresstest/plans
     * stores the available plans in "plans"
     */
    public void updatePlans() {

        lastPlanUpdate = System.currentTimeMillis();

        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(BUCKET_NAME)
                .withPrefix("stresstest/plans");

        List<S3ObjectSummary> objects = s3client.listObjects(listObjectsRequest).getObjectSummaries();

        if(objects == null) plans = new ArrayList<>();

        plans = objects.stream().map(S3ObjectSummary::getKey)
                .filter(o -> { String[] path = o.split("/"); return path.length > 2; }) // ignore folder
                .collect(Collectors.toList());

    }

    /**
     * starts all JMeter master instances on AWS (normally just one)
     */
    public String startMaster() {
        AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG).forEach(InstanceInfo::start);
        lastAWSUpdate = 0;
        return "stresstest-control";
    }

    /**
     * stops all JMeter master instances on AWS (normally just one)
     */
    public String stopMaster() {
        AWSInstances.loadAvailableInstances(JMETER_MASTER_AWS_TAG).forEach(InstanceInfo::stop);
        lastAWSUpdate = 0;
        return "stresstest-control";
    }

    /**
     * gets an advice for the JMeter Master
     * @return
     *          Advice String to insert as JMeter Master
     */
    public String getMasterAdvice() {
        List<Instance> instances = AWSInstances.loadAvailableInstances();
        InstanceInfo master = AWSInstances.findInstance(JMETER_MASTER_AWS_TAG, instances).get(0);
        return master.getPrivateIP(); // !!! Only works on AWS not local
    }

    /**
     * starts all JMeter server instances on AWS (normally about 3)
     */
    public String startServer() {
        AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG).forEach(InstanceInfo::start);
        lastAWSUpdate = 0;
        return "stresstest-control";
    }

    /**
     * stops all JMeter server instances on AWS (normally about 3)
     */
    public String stopServer() {
        AWSInstances.loadAvailableInstances(JMETER_SERVER_AWS_TAG).forEach(InstanceInfo::stop);
        lastAWSUpdate = 0;
        return "stresstest-control";
    }

    /**
     * gets an advice for the JMeter Servers
     * @return
     *          Advice String to insert as JMeter Servers
     */
    public String getServersAdvice() {
        List<Instance> instances = AWSInstances.loadAvailableInstances();
        List<InstanceInfo> server = AWSInstances.findInstance(JMETER_SERVER_AWS_TAG, instances);
        return server.stream().map(InstanceInfo::getPrivateIP).collect(Collectors.joining(","));
    }

    /**
     * starts all instances and select the default servers
     */
    public void autoConfig() {
        startMaster();
        startServer();
        setIp(getMasterAdvice());
        setRemote(getServersAdvice());
    }

    /**
     * checks if the set IPs for Master and Server are available
     * @param exception
     *          if true and not all instances are online an exception is thrown.
     * @return
     *          true if all is online
     *          false or an exception if not
     */
    public boolean selectedInstancesAvailable(boolean exception) {
        try {
            selectedInstancesAvailable();
            return true;
        } catch (RuntimeException e) {
            if(exception) throw e;
            else return false;
        }
    }

    private void selectedInstancesAvailable() {
        List<Instance> instances = AWSInstances.loadAvailableInstances();
        if(!AWSInstances.isIPRunning(ip, instances)) throw new RuntimeException("JMeter Master with IP " + ip + " is not running");
        Arrays.stream(remote.split(",")).forEach(serverIP -> {
            if (!AWSInstances.isIPRunning(serverIP, instances))
                throw new RuntimeException("JMeter Server with IP " + serverIP + " is not running");
        });
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

        if(testIsRunning) {
            throw new RuntimeException("A stresstest is already running. Please wait until it is finished.");
        }
        testIsRunning = true;

        try { // exceptions will be thrown after setting running to false

            // validate input data
            if(ip == null || ip.equals("")) {
                throw new RuntimeException("No IP for JMeter Master set");
            }
            if(remote == null || remote.equals("")) {
                throw new RuntimeException("No IP for JMeter Server set. Use private IP comma separated (without space)");
            }
            try {
                if(testPlanPort != null && !testPlanPort.equals(""))Integer.parseInt(testPlanPort);
            } catch (NumberFormatException e) {
                throw new RuntimeException("The destination port has to be an Integer");
            }
            try {
                if(testPlanThreads != null && !testPlanThreads.equals(""))Integer.parseInt(testPlanThreads);
            } catch (NumberFormatException e) {
                throw new RuntimeException("The threads have to be an Integer");
            }
            try {
                if(testPlanLoops != null && !testPlanLoops.equals(""))Integer.parseInt(testPlanLoops);
            } catch (NumberFormatException e) {
                throw new RuntimeException("The loops have to be an Integer");
            }

            // prepare output
            serverResponse = "Starting stresstest";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentTime = dateFormat.format(new Date());
            serverResponse+= "<br />Starting at: " + currentTime;

            serverResponse+= "<br />Master: " + ip;
            serverResponse+= "<br />Server: " + remote;
            serverResponse+= "<br />Plan: " + plan;
            serverResponse+= "<br />Destination Server: " + testPlanIp;
            serverResponse+= "<br />Destination Port: " + testPlanPort;
            serverResponse+= "<br />Threads: " + testPlanThreads;
            serverResponse+= "<br />Loops: " + testPlanLoops;

            // Check instances to be running
            selectedInstancesAvailable(true);

            // start test
            SSHExecutor ssh = new SSHExecutor(ip, user);

            // get JMETER_HOME
            InputStream inputStream = ssh.execute("echo ${JMETER_HOME}");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            jmeterHome = bufferedReader.readLine();
            serverResponse += "<br />JMETER_HOME: " + jmeterHome;

            // copy selected testplan to JMeter Master
            AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());
            S3Object s3Object = s3client.getObject(new GetObjectRequest(BUCKET_NAME, plan));
            InputStream stream = s3Object.getObjectContent();

            // insert custom data into test plan
            TestplanConfig testplanConfig = new TestplanConfig();
            testplanConfig.setIp(testPlanIp)
                    .setPort(testPlanPort)
                    .setThreads(testPlanThreads)
                    .setLoops(testPlanLoops);

            try {
                finalTestPlan = testplanConfig.parseTestPlan(stream);
            } catch (ParserConfigurationException | SAXException | TransformerException e) {
                throw new RuntimeException("The selected test plan can not be modified with your custom data. Leave the fields blank or make sure that the testplan contains a 'user defined variable' as first element, containing the changed vars");
            }
            stream = new ByteArrayInputStream(finalTestPlan.getBytes(StandardCharsets.UTF_8));

            //ssh.upload("/home/hendrik/jmeter", "neo4jTest.jmx", stream);
            ssh.upload(jmeterHome + "/jmeter", "test.jmx", stream);

            // run the test!
            //String command = "cd " + jmeterHome + "/docker/jmeter-master/; echo " + password + " | sudo -S " + jmeterHome + "/docker/jmeter-master/run_test.sh " + remote + ";";
            String command = jmeterHome + "/docker/jmeter-master/run_test.sh " + remote + ";";
            serverResponse += "<br />" + command;
            BufferedReader in = new BufferedReader(new InputStreamReader(ssh.execute(command)));

            // handle console output from JMeter Master in another thread
            new Thread() {
                public void run() {

                    String msg = null;
                    try {
                        while ((msg = in.readLine()) != null) {
                            serverResponse += "<br />" + msg;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // close connections and remove tmp files
                    ssh.clean();
                    serverResponse += "<br />" + "Copy results to s3 will be done by JMeter Master";


                    try {
                        copyResultsToS3();
                        serverResponse += "<br />" + "Results saved on s3";
                    } catch (JSchException | SftpException | IOException e) {
                        e.printStackTrace();
                        serverResponse += "<br />" + "!!!! Results could not be stored on S3 !!!!";
                    }

                    serverResponse += "<br />" + "Finished Stresstest";
                    testIsRunning = false;
                }
            }.start();

        } catch (Exception e) {
            // if any exception is throws the test will not be finished -> remember that nothing is running and throw the exception again
            testIsRunning = false;
            throw e;
        }
    }

    /**
     * stores the result file on s3
     * @throws JSchException
     * @throws SftpException
     * @throws IOException
     */
    private void copyResultsToS3() throws JSchException, SftpException, IOException {

        /* upload of tar will be done by jmeter master
        // copy result file to tmp file
        SSHExecutor ssh = new SSHExecutor(ip, user);
        File tmpFile = ssh.download(jmeterHome + RESULT_TAR);

        // Map file to byte array
        Path path = Paths.get(tmpFile.getAbsolutePath());
        byte[] data = Files.readAllBytes(path);
        tmpFile.delete();
        */

        // Upload data to s3
        AmazonS3 s3client = new AmazonS3Client(new AWSCredentials());
        //ObjectMetadata objectMetadata = new ObjectMetadata();
        //objectMetadata.setContentLength(data.length);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentTime = dateFormat.format(new Date());
        /*s3client.putObject(
                new PutObjectRequest(
                        BUCKET_NAME,
                        "stresstest/results/" + currentTime + "/log.tar",
                        new ByteArrayInputStream(data),
                        objectMetadata
                ));*/

        // create file with global test data
        File settings = File.createTempFile("settings", ".txt");
        PrintWriter writer = new PrintWriter(settings, "UTF-8");
        writer.println("Testplan: " + plan);
        writer.println("Master: " + ip);
        writer.println("Remote: " + remote);
        writer.println("User: " + user);
        writer.println("Time: " + currentTime);
        writer.println("Timestamp: " + System.currentTimeMillis());
        writer.println("");
        writer.println("----------------------------------------------------------");
        writer.println("Log from run");
        writer.println("");
        writer.println(serverResponse.replaceAll("<br />", "\n"));
        writer.close();

        // copy global test data to s3
        Path path = Paths.get(settings.getAbsolutePath());
        byte[] data = Files.readAllBytes(path);
        settings.delete();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(data.length);
        s3client.putObject(
                new PutObjectRequest(
                        BUCKET_NAME,
                        "stresstest/results/" + currentTime + "/settings.txt",
                        new ByteArrayInputStream(data),
                        objectMetadata
                ));

        // save testplan
        data = finalTestPlan.getBytes();
        objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(data.length);
        s3client.putObject(
                new PutObjectRequest(
                        BUCKET_NAME,
                        "stresstest/results/" + currentTime + "/testPlan.jmx",
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
        getResultFile(jmeterHome + RESULT_TAR);
    }

    /**
     * download the small result file with only minimized data and svg-diagrams
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     */
    public void getResultMin() throws SftpException, JSchException, IOException {
        getResultFile(jmeterHome + RESULT_TAR_MIN);
    }

    /**
     * download global jtl file
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     */
    public void getResultJtl() throws SftpException, JSchException, IOException {
        getResultFile(jmeterHome + RESULT_JTL, "jtl.jtl");
    }

    /**
     * download global jtl file
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     * @return
     *          The local copy of the jtl file
     */
    public File getResultJtl(boolean getFileObject) throws SftpException, JSchException, IOException {
        return copyResultFile(jmeterHome + RESULT_JTL);
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
        getResultFile(filename, "log.tar");
    }

    /**
     * copy the selected file to the deployer and returns the local file
     * @param filename
     *          The file on the server to be downloaded
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     */
    private File copyResultFile(String filename) throws SftpException, JSchException, IOException {
        SSHExecutor ssh = new SSHExecutor(ip, user);

        File tmpFile = ssh.download(filename);

        return tmpFile;
    }

    /**
     * downloads the passed file from the server and let the user save it
     * @param filename
     *          The file on the server to be downloaded
     * @param finalName
     *          The filename to be displayed on download
     * @throws SftpException
     * @throws JSchException
     * @throws IOException
     */
    private void getResultFile(String filename, String finalName) throws SftpException, JSchException, IOException {
        File tmpFile = copyResultFile(filename);

        // parse tmp file to byte array to return it to client
        Path path = Paths.get(tmpFile.getAbsolutePath());
        byte[] data = Files.readAllBytes(path);
        tmpFile.delete();

        // return file
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseHeader("Content-Type", "application/tar");
        externalContext.setResponseHeader("Content-Length", String.valueOf(data.length));
        externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + finalName + "\"");
        try {
            externalContext.getResponseOutputStream().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        facesContext.responseComplete();
    }

}
