package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.control.ApplicationDockerPackage;
import com.poolingpeople.deployer.dockerapi.boundary.ContainerInfo;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeployerFacadeTestUT {

    private DeployerFacade cut;

    @Before
    public void setUp(){
        cut = new DeployerFacade();
        cut.dockerApi = mock(DockerApi.class);
        cut.applicationDockerPackage = new ApplicationDockerPackage();
        cut.clusterConfig = new ClusterConfig();
        cut.versionsApi = new VersionsApi();
    }

    @Test
    public void testGetAvailableCluster(){

        List<ContainerInfo> containerInfos = new ArrayList<>();
        List<String> names = new ArrayList<>();
        names.add("/1-wf-0.0.1-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/1-wf-0.0.1-albert-test.poolingpeople.com/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        names.add("/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/4-wf-0.0.1-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/4-wf-0.0.1-albert-test.poolingpeople.com/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        names.add("/4-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/2-wf-0.0.1-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/2-wf-0.0.1-albert-test.poolingpeople.com/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        names.add("/2-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        when(cut.dockerApi.listContainers()).thenReturn(containerInfos);

        assertThat(cut.getAvailableCluster(), is(3));

    }

    @Test
    public void testGetActiveContainersNames() throws Exception {

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

}