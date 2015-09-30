package com.poolingpeople.deployer.boundary;

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

    @GET
    public String info() {
        return "Rest is running";
    }


    @POST
    @Path("deploy/{subdomain}/{dbSnapshotName}")
    @Consumes("application/x-webarchive")
    public String deployApplication(@PathParam("subdomain") String subdomain, @PathParam("dbSnapshotName") String dbSnapshotName, byte[] warFile) {
        String version = "restDeployment"; // needed for container naming
        boolean overwrite = true;
        String appEnvironment = "test"; // deployment over rest is only possible in test environment

        facade.deploy(warFile, version, subdomain, dbSnapshotName, overwrite, appEnvironment);

        return "I finished a deployment";
    }

}
