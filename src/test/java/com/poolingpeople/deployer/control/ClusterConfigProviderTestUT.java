package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.dockerapi.boundary.ContainerInfo;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.entity.ClusterConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClusterConfigProviderTestUT {

    ClusterConfigProvider cut;

    @Before
    public void setUp() throws Exception {
        cut = new ClusterConfigProvider();
        cut.dockerApi = mock(DockerApi.class);
    }

    @Test
    public void testGetCurrentClusters() throws Exception {

        List<ContainerInfo> containerInfos = new ArrayList<>();
        List<String> names = new ArrayList<>();
        names.add("/1-wf-0.0.1-albert1-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/1-wf-0.0.1-albert-test.poolingpeople.com/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        names.add("/1-neo4j-pivotal15_01_2015-albert1-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/4-wf-0.0.1-albert2-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/4-wf-0.0.1-albert-test.poolingpeople.com/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        names.add("/4-neo4j-pivotal15_01_2015-albert2-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/2-wf-0.0.1-albert3-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        names = new ArrayList<>();
        names.add("/2-wf-0.0.1-albert-test.poolingpeople.com/1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com");
        names.add("/2-neo4j-pivotal15_01_2015-albert3-test.poolingpeople.com");
        containerInfos.add(new ContainerInfo().setNames(names));

        when(cut.dockerApi.listContainers()).thenReturn(containerInfos);

        Collection<ClusterConfig> clusters = cut.getCurrentClusters("");

        assertThat(clusters.size(), is(3));

    }
}