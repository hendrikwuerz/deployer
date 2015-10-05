package com.poolingpeople.deployer.stresstest.boundary;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.poolingpeople.deployer.boundary.DeployerFacade;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by hendrik on 30.09.15.
 */
@Path("/stresstest") // set the path to REST web services
public class StresstestRest {

    @Inject
    StresstestEndPoint stress;

    @GET
    public String info() {
        return "Rest is running.";
    }

    /**
     * start the JMeter Master and Sever instances on aws
     * @return "ok" if started
     */
    @PUT
    @Path("start/all")
    public String startAll() {
        stress.startMaster();
        stress.startServer();
        return "ok";
    }

    /**
     * start the JMeter Master instance on aws
     * @return "ok" if started
     */
    @PUT
    @Path("start/master")
    public String startMaster() {
        stress.startMaster();
        return "ok";
    }

    /**
     * start the JMeter Server instances on aws
     * @return "ok" if started
     */
    @PUT
    @Path("start/server")
    public String startServer() {
        stress.startServer();
        return "ok";
    }

    /**
     * start the JMeter Master and Sever instances on aws
     * @return "ok" if started
     */
    @PUT
    @Path("stop/all")
    public String stopAll() {
        stress.stopMaster();
        stress.stopServer();
        return "ok";
    }

    /**
     * stop the JMeter Master instance on aws
     * @return "ok" if stopped
     */
    @PUT
    @Path("stop/master")
    public String stopMaster() {
        stress.stopMaster();
        return "ok";
    }

    /**
     * stop the JMeter Server instances on aws
     * @return "ok" if stopped
     */
    @PUT
    @Path("stop/server")
    public String stopServer() {
        stress.stopServer();
        return "ok";
    }

    /**
     * set the JMeter master
     * @return "ok" if set
     */
    @PUT
    @Path("/master")
    public String setMaster(String master) {
        stress.setIp(master);
        return "ok";
    }

    /**
     * get the JMeter master
     * @return the JMeter Master
     */
    @GET
    @Path("/master")
    public String getMaster() {
        return stress.getIp();
    }

    /**
     * set the JMeter servers
     * @return "ok" if set
     */
    @PUT
    @Path("/server")
    public String setServer(String server) {
        stress.setRemote(server);
        return "ok";
    }

    /**
     * get the JMeter servers
     * @return the JMeter Server
     */
    @GET
    @Path("/server")
    public String getServer() {
        return stress.getRemote();
    }

    /**
     * set the destination application IP
     * @return "ok" if set
     */
    @PUT
    @Path("/destination/ip")
    public String setDestinationIp(String ip) {
        stress.setTestPlanIp(ip);
        return "ok";
    }

    /**
     * get the destination application IP
     * @return the destination application IP
     */
    @GET
    @Path("/destination/ip")
    public String getDestinationIp() {
        return stress.getTestPlanIp();
    }

    /**
     * set the destination application port
     * @return "ok" if set
     */
    @PUT
    @Path("/destination/port")
    public String setDestinationPort(String port) {
        stress.setTestPlanPort(port);
        return "ok";
    }

    /**
     * get the port of the destination application
     * @return the destination port
     */
    @GET
    @Path("/destination/port")
    public String getDestinationPort() {
        return stress.getTestPlanPort();
    }

    /**
     * set amount of threads which the test plan will use
     * @return "ok" if set
     */
    @PUT
    @Path("/destination/threads")
    public String setDestinationThreads(String threads) {
        stress.setTestPlanThreads(threads);
        return "ok";
    }

    /**
     * get amount of threads which the test plan will use
     * @return the amount of threads
     */
    @GET
    @Path("/destination/threads")
    public String getDestinationThreads() {
        return stress.getTestPlanThreads();
    }

    /**
     * set amount of loops which the test plan will use
     * @return "ok" if set
     */
    @PUT
    @Path("/destination/loops")
    public String setDestinationLoops(String loops) {
        stress.setTestPlanLoops(loops);
        return "ok";
    }

    /**
     * get amount of loops which the test plan will use
     * @return the amount of loops
     */
    @GET
    @Path("/destination/loops")
    public String getDestinationLoops() {
        return stress.getTestPlanLoops();
    }

    /**
     * starts all instances and select the default servers
     * @return "ok" if starting
     */
    @PUT
    @Path("prepare")
    public String prepare() {
        stress.autoConfig();
        return "ok";
    }

    /**
     * starts all instances and select the default servers
     * @return "ok" if starting
     */
    @GET
    @Path("available")
    public String available(String master) {
        if(stress.selectedInstancesAvailable(false)) {
            return "yes";
        } else {
            return "no";
        }
    }

    /**
     * start the test with the set data
     * @param testplan
     *          The testplan to be executed
     * @return "ok" if test is started
     * @throws InterruptedException
     * @throws JSchException
     * @throws SftpException
     * @throws IOException
     */
    @POST
    @Path("run/{testplan}")
    public String runTest(@PathParam("testplan") String testplan) throws InterruptedException, JSchException, SftpException, IOException {
        stress.setPlan("stresstest/plans/" + testplan);
        stress.runTest();

        return "ok";

    }

    /**
     * starts all instances and select the default servers
     * @return "ok" if starting
     */
    @GET
    @Path("status")
    public String status() {
        return stress.getServerResponse();
    }

    /**
     * checks if a test is running at the moment
     * @return "yes" if a test is running. "false" otherwise
     */
    @GET
    @Path("running")
    public String isRunning() {
        return (stress.isTestRunning() ? "yes" : "no");
    }

    /**
     * set the running status of a test. If a test is running, a new one can not be started.
     * If the deployer has the wrong status this can be corrected here.
     * !!! Normally this method should newver be called
     * @return "ok" if status is set
     */
    @PUT
    @Path("running")
    public String setIsRunning(String running) {
        stress.setTestIsRunning(running.equals("true"));
        return "ok";
    }


    /**
     * starts all instances and select the default servers
     * @return "ok" if starting
     */
    @GET
    @Path("config")
    public String getConfig() {
        return  "JMeter Master:    " + stress.getIp() + System.lineSeparator() +
                "JMeter Server:    " + stress.getRemote() + System.lineSeparator() +
                "Testplan:         " + stress.getPlan() + System.lineSeparator() +
                "Destination IP:   " + stress.getTestPlanIp() + System.lineSeparator() +
                "Destination Port: " + stress.getTestPlanPort() + System.lineSeparator() +
                "Threads:          " + stress.getTestPlanThreads() + System.lineSeparator() +
                "Loops:            " + stress.getTestPlanLoops() + System.lineSeparator();
    }


    @GET
    @Path("get/jtl")
    public File getJtl() throws SftpException, JSchException, IOException {
        return stress.getResultJtl(true);
    }

}
