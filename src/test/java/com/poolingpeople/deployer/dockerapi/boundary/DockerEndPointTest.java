package com.poolingpeople.deployer.dockerapi.boundary;

import org.junit.Test;

import static org.junit.Assert.*;

public class DockerEndPointTest {

    @Test
    public void testGetURI() throws Exception {
        DockerEndPoint endPoint = new DockerEndPoint("poolingpeople.com", 5555, "http");
        String expected = "http://poolingpeople.com:5555";
        assertEquals(expected, endPoint.getURI());
    }
}