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
    @Path("deploy")
    @Consumes("application/x-webarchive")
    public String deployApplication(byte[] warFile) {
        String version = "0.1.5";
        String subdomain = "sub";
        String dbSnapshotName = "empty.tar";
        boolean overwrite = true;
        String appEnvironment = "test";

        facade.deploy(warFile, version, subdomain, dbSnapshotName, overwrite, appEnvironment);

        return "I finished a deployment";
    }

}
