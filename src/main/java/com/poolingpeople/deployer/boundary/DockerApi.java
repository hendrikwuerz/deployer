package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.control.ApplicationDockerPackage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import static javax.ws.rs.client.Entity.entity;

/**
 * Created by alacambra on 03.02.15.
 */
public class DockerApi {

    @Inject
    ApplicationDockerPackage aPackage;

    String endPoint = "http://localhost:5555";

    public String getDockerInfo(){
        return "";
    }

    public String buildImage(String imageName){
        String url = endPoint + "/build?t={imageName}";

        Client client = ClientBuilder.newClient();
        Response response = client
                .target(url)
                .resolveTemplate("imageName", imageName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(entity(new ByteArrayInputStream(aPackage.prepareTarStream().getBytes()), "application/tar"), Response.class);

        String r = response.readEntity(String.class);
        return r;
    }

    public String deleteImage(String imageName){

        String url = endPoint + "/images/{imageName}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("imageName", imageName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        String r = response.readEntity(String.class);
        return r;
    }

    public String listImage(){

        String url = endPoint + "/images/json?all=0";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get();

        String r = response.readEntity(String.class);
        return r;

    }

    public String createContainer(ContainerCreateBodyBuilder bodyBuilder){
        String url = endPoint + "/containers/create";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .request()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(Entity.json(bodyBuilder.getObjectBuilder().build()));

        String r = response.readEntity(String.class);
        return r;

    }

    public String startContainer(String containerId){
        String url = endPoint + "/containers/{containerId}/start";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .post(Entity.json(""));

        String r = response.readEntity(String.class);
        return r;

    }

    public String stopContainer(String containerId){
        String url = endPoint + "/containers/{containerId}/stop";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .post(Entity.json(""));

        String r = response.readEntity(String.class);
        return r;

    }

    public String killContainer(String containerId){
        String url = endPoint + "/containers/{containerId}/kill";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .post(Entity.json(""));

        String r = response.readEntity(String.class);
        return r;

    }

    public String removeContainer(String containerId){
        String url = endPoint + "/containers/{containerId}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .delete();

        String r = response.readEntity(String.class);
        return r;

    }

    public Collection<ContainerInfo> listContainers(){
        String url = endPoint + "/images/json?all=0";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        InputStream r = response.readEntity(InputStream.class);
        ContainersInfoReader reader = new ContainersInfoReader();
        Collection<ContainerInfo> containers = reader.getContainers(r);

        return containers;

    }



}
