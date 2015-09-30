package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.dockerapi.boundary.DockerEndPoint;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

/**
 * Created by hendrik on 30.09.15.
 */

@Path("/deployer") // set the path to REST web services
public class DeployerRest extends Application {

    @Inject
    DeployerFacade facade;

    @Inject
    DockerEndPoint endPointProvider;

    @GET
    public String info() {
        return "Rest is running. Use POST deploy/{host}/{subdomain}/{dbSnapshotName} to deploy a war";
    }

    @POST
    @Path("info")
    public String hello(String message) {
        return message;
    }


    @POST
    @Path("deploy/{host}/{subdomain}/{dbSnapshotName}")
    @Consumes("application/x-webarchive")
    public String deployApplication(@PathParam("host") String host, @PathParam("subdomain") String subdomain, @PathParam("dbSnapshotName") String dbSnapshotName, byte[] warFile) {
        String version = "restDeployment"; // needed for container naming
        boolean overwrite = true;
        String appEnvironment = "test"; // deployment over rest is only possible in test environment

        endPointProvider.setHost(host);

        facade.deploy(warFile, version, subdomain, dbSnapshotName, overwrite, appEnvironment);

        return "I finished a deployment";
    }

}
