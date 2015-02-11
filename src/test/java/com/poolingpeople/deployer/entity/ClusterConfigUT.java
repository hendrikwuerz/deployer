package com.poolingpeople.deployer.entity;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClusterConfigUT {

    ClusterConfig cut;

    @Before
    public void setUp(){
        cut = new ClusterConfig();
    }

    @Test
    public void testLoadFromContainerName() throws Exception {
        String wfContainerName = "1-wf-0.0.1-albert-test.poolingpeople.com";
        String neo4jContainerName = "1-neo4j-pivotal15_01_2015-albert-test.poolingpeople.com";

        cut.loadFromContainerName(wfContainerName);
        cut.loadFromContainerName(neo4jContainerName);

        assertThat(cut.getAppVersion(), Is.is("0.0.1"));
        assertThat(cut.getDbScenario(), Is.is("pivotal15_01_2015"));
        assertThat(cut.getConcretDomain(), Is.is("albert"));
        assertThat(cut.getServerDomain(), Is.is("test.poolingpeople.com"));
    }
}