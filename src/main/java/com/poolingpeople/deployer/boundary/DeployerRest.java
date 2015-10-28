package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.dockerapi.boundary.DockerEndPoint;
import com.poolingpeople.deployer.scenario.boundary.AWSInstances;
import com.poolingpeople.deployer.scenario.boundary.InstanceInfo;
import com.poolingpeople.deployer.stresstest.boundary.StresstestEndPoint;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by hendrik on 30.09.15.
 */

@Path("/deployer") // set the path to REST web services
public class DeployerRest {

    @Inject
    DeployerFacade facade;

    @Inject
    DockerEndPoint endPointProvider;

    // only instances with the following tags can be controlled over REST
    List<String> allowedTags = Arrays.asList("deployer", StresstestEndPoint.JMETER_MASTER_AWS_TAG, StresstestEndPoint.JMETER_SERVER_AWS_TAG);

    @GET
    public String info() {
        return "Rest is running. Use POST deploy/{host}/{subdomain}/{dbSnapshotName} to deploy a war";
    }

    @POST
    @Path("hello")
    public String hello(String message) {
        return message;
    }

    /**
     * starts an aws instance
     * @param name
     *          The "name" of the instance (set in the tag with key=="name")
     * @return
     *          "ok" if started
     */
    @PUT
    @Path("start/{name}")
    public String startInstance(@PathParam("name") String name) {
        Optional<InstanceInfo> foundInstance = AWSInstances.loadAvailableInstances(allowedTags).stream()
                .filter(instance -> instance.getName().equals(name))
                .findAny();
        if(foundInstance.isPresent()) {
            foundInstance.get().start();
            return "ok";
        } else {
            throw new NotFoundException("Not found");
        }
    }

    /**
     * stops an aws instance
     * @param name
     *          The "name" of the instance (set in the tag with key=="name")
     * @return
     *          "ok" if started
     */
    @PUT
    @Path("stop/{name}")
    public String stopInstance(@PathParam("name") String name) {
        Optional<InstanceInfo> foundInstance = AWSInstances.loadAvailableInstances(allowedTags).stream()
                .filter(instance -> instance.getName().equals(name))
                .findAny();
        if(foundInstance.isPresent()) {
            foundInstance.get().stop();
            return "ok";
        } else {
            throw new NotFoundException("Not found");
        }
    }

    /**
     * gets the status of an aws instance
     * @param name
     *          The "name" of the instance (set in the tag with key=="name")
     * @return
     *          "ok" if started
     */
    @GET
    @Path("status/{name}")
    public String statusInstance(@PathParam("name") String name) {
        Optional<InstanceInfo> foundInstance = AWSInstances.loadAvailableInstances(allowedTags).stream()
                .filter(instance -> instance.getName().equals(name))
                .findAny();
        if(foundInstance.isPresent()) {
            return foundInstance.get().getStatus();
        } else {
            throw new NotFoundException("Not found");
        }
    }

    @POST
    @Path("deploy/{host}/{subdomain}/{dbSnapshotName}")
    @Consumes("application/x-webarchive")
    public String deployApplication(@PathParam("host") String host, @PathParam("subdomain") String subdomain, @PathParam("dbSnapshotName") String dbSnapshotName, byte[] warFile) throws IOException {
        String version = "restDeployment"; // needed for container naming
        boolean overwrite = true;
        String appEnvironment = "test"; // deployment over rest is only possible in test environment

        endPointProvider.setHost(host);

        facade.deploy(warFile, version, subdomain, dbSnapshotName, overwrite, appEnvironment);

        return "I finished a deployment";
    }

    @POST
    @Path("deploy/{host}/{subdomain}/{dbSnapshotName}/{area}/{version}")
    public String deployApplication(@PathParam("host") String host, @PathParam("subdomain") String subdomain, @PathParam("dbSnapshotName") String dbSnapshotName, @PathParam("area") String area, @PathParam("version") String version) throws IOException {

        if(!area.equals("releases") && !area.equals("snapshots")) {
            throw new RuntimeException("Unknown area passed. Only 'releases' or 'snapshots' are allowed here");
        }

        boolean forceDownload = true;
        boolean overwrite = true;
        String appEnvironment = "test"; // deployment over rest is only possible in test environment

        endPointProvider.setHost(host);

        facade.deploy(version, subdomain, dbSnapshotName, area, forceDownload, overwrite, appEnvironment);

        return "I finished a deployment";
    }

}
