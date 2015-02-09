package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.docker.boundary.CreateContainerBodyBuilder;
import com.poolingpeople.deployer.docker.boundary.DockerApi;
import org.junit.Before;
import org.junit.Test;

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
        String r = cut.deleteImage("myNginx");
        System.out.println(r);

    }

    @Test
    public void testListImage() throws Exception {

        System.out.println(cut.listImage());

    }

    @Test
    public void testCreateContainer() throws Exception {
        CreateContainerBodyBuilder builder = new CreateContainerBodyBuilder();
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
}