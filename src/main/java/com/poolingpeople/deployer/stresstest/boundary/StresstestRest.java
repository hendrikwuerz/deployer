package com.poolingpeople.deployer.stresstest.boundary;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.poolingpeople.deployer.boundary.DeployerFacade;

import javax.inject.Inject;
import javax.ws.rs.*;
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
        stress.setIp(server);
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



}
