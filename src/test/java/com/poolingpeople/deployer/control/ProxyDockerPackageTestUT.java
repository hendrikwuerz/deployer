package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.entity.ClusterConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyDockerPackageTestUT {

    ProxyDockerPackage cut;

    @Before
    public void setUp() throws Exception {
        cut = new ProxyDockerPackage();
        cut.clusterConfigProvider = mock(ClusterConfigProvider.class);
        cut.clusterConfig = new ClusterConfig().setServerDomain("test.com");
    }

    @Test
    public void testGenerateTar() throws Exception {
        Collection<ClusterConfig> configs = new ArrayList<>();
        configs.add(
                new ClusterConfig().setAppBaseName("rest")
                        .setAppVersion("0.0.1")
                        .setNeo4jId("neoId")
                        .setConcretDomain("arne")
                        .setPortPrefix("1")
                        .setServerDomain("test.com")
                        .setWildflyId("wfid")
        );

        configs.add(
                new ClusterConfig().setAppBaseName("rest")
                        .setAppVersion("0.0.2")
                        .setNeo4jId("neoId2")
                        .setConcretDomain("al")
                        .setPortPrefix("2")
                        .setServerDomain("test.com")
                        .setWildflyId("wfid2")
        );

        when(cut.clusterConfigProvider.getCurrentClusters("test.com")).thenReturn(configs);
        cut.prepareTarStream();
        assertThat(cut.getBytes().length, is(not(0)));

    }




    @Test
    public void testAddResources() throws Exception {

    }

    @Test
    public void testAddNeo4jFile() throws Exception {

    }

    @Test
    public void testAddWfConsoleFile() throws Exception {

    }

    @Test
    public void testAddWebappFile() throws Exception {

    }

    @Test
    public void testReplaceClusterBars() throws Exception {

    }
}