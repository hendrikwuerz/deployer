package com.poolingpeople.deployer.dockerapi.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.dockerapi.boundary.CreateContainerBodyWriter;
import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.mockito.Mockito.mock;

public class DockerApiTestIT {

    DockerApi cut;
    VersionsApi versionsApi = new VersionsApi();

    @Before
    public void setUp(){

        cut = new DockerApi();
//        cut.aPackage = new ApplicationDockerPackage();

    }

    @Test
    public void testBuildImage() throws Exception {
//
//        String r = cut.buildImage("testImage");
//        System.out.println(r);
    }

    @Test
    public void testDeleteImage() throws Exception {
        cut.deleteImage("myNginx");
    }

    @Test
    public void testListImage() throws Exception {

        System.out.println(cut.listImage());

    }

    @Test
    public void testCreateContainer() throws Exception {
        CreateContainerBodyWriter builder = new CreateContainerBodyWriter();
        builder.setImage("4d3dc25426d6").exposeTcpPort(8080);
        System.out.println(cut.createContainer(builder, null));
    }

    @Test
    public void testStartContainer() throws Exception {
//        System.out.println(cut.startContainer("abedefd9e18325d7865b9dd115c8207b403579d8444c9bd720b2607c7e1d691a"));
    }

    @Test
    public void testStopContainer() throws Exception {

    }

    @Test
    public void testKillContainer() throws Exception {

    }

    @Test
    public void testRemoveContainer() throws Exception {

    }

    @Test
    public void testListContainer() throws Exception {

    }

//    @Test
//    public void testCopyFiles() throws IOException {
//        cut.endPoint = "http://prod.poolingpeople.com:5555";
//        InputStream inputStream = cut.copyFiles("3224415425", "/var/lib/neo4j/data/graph.db/");
//        FileOutputStream outputStream = new FileOutputStream("/home/alacambra/deleteme.tar");
//        IOUtils.copy(inputStream, outputStream);
//
//    }
}