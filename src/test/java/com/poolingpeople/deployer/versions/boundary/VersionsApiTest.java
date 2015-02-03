package com.poolingpeople.deployer.versions.boundary;

import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class VersionsApiTest {

    VersionsApi cut;

    @Before
    public void setUp(){
        cut = new VersionsApi();
    }

    @Test
    public void testParseVersions() throws Exception {

        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("nexus-snapshots-response.json");
        Collection<String> versions = cut.parseVersions(stream);
        System.out.println(versions);
        assertThat(versions, containsInAnyOrder("0.0.2-SNAPSHOT", "0.0.1-SNAPSHOT", "0.0.0-SNAPSHOT"));
    }

}