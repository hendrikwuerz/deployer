package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.control.ApplicationDockerPackage;
import com.poolingpeople.deployer.docker.boundary.ContainerInfo;
import com.poolingpeople.deployer.docker.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void testGetAvailableCluster(){

        cut.dockerApi = mock(DockerApi.class);
        List<ContainerInfo> containerInfos = new ArrayList<>();
        List<String> names = new ArrayList<>();
        names.add("/1-wf-0.0.1-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/1-wf-0.0.1-albert-test.poolingpeople.com/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        names.add("/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        when(cut.dockerApi.listContainers()).thenReturn(containerInfos);

        Collection<String> o = cut.getActiveContainersNames();
        System.out.println(o);

        assertThat(o, containsInAnyOrder(
                "1-wf-0.0.1-albert-test.poolingpeople.com",
                "1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com"));
    }

    @Test
    public void testDeploy(){
        cut.deploy("0.0.1", "test", null);
    }
}