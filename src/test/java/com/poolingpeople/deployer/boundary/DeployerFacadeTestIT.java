package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.control.ApplicationDockerPackage;
import com.poolingpeople.deployer.docker.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;
import org.junit.Before;
import org.junit.Test;

public class DeployerFacadeTestIT {

    DeployerFacade cut;

    @Before
    public void setUp(){
        cut = new DeployerFacade();
        cut.dockerApi = new DockerApi();
        cut.applicationDockerPackage = new ApplicationDockerPackage();
        cut.clusterConfig = new ClusterConfig();
        cut.versionsApi = new VersionsApi();
    }


    @Test
    public void testDeploy(){

        cut.deploy("0.0.1", "test");
    }
}