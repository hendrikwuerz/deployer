package com.poolingpeople.deployer.docker.boundary;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static javax.ws.rs.client.Entity.entity;

/**
 * Created by alacambra on 03.02.15.
 */
public class DockerApi implements Serializable{

    String endPoint = "http://localhost:5555";

    Logger logger = Logger.getLogger(this.getClass().getName());

    public String getDockerInfo(){
        return "";
    }

    public String buildImage(String imageName, byte[] tarBytes){
        String url = endPoint + "/build?t={imageName}";

        Client client = ClientBuilder.newClient();
        Response response = client
                .target(url)
                .resolveTemplate("imageName", imageName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(entity(new ByteArrayInputStream(tarBytes), "application/tar"), Response.class);

        if(response.getStatus() != Response.Status.OK.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }

        String r = response.readEntity(String.class);

        if (r.contains("errorDetail")){
            throw new RuntimeException(r);
        }

        logger.info(r);
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

    public String createContainer(CreateContainerBodyBuilder bodyBuilder, String name){
        String url = endPoint + "/containers/create?name={name}";
        Client client = ClientBuilder.newClient();

        String body = bodyBuilder.getObjectBuilder().build().toString();

        logger.info("creating container with body: " + body);

        Response response = client
                .target(url)
                .resolveTemplate("name", Optional.ofNullable(name).orElse(UUID.randomUUID().toString()))
                .request()
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        InputStream r = response.readEntity(InputStream.class);

        if(response.getStatus() != Response.Status.CREATED.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }

        JsonObject object = Json.createReader(r).readObject();

        return ((JsonString)object.get("Id")).getString();

    }

    public void startContainer(String containerId){
        String url = endPoint + "/containers/{containerId}/start";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .post(Entity.json(""));

        if(response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }

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

    public String getContainersLogs(String containerId, int tail){

        String url = endPoint + "/containers/{containerId}/logs?stderr=1&stdout=1&timestamps=1&follow=0&tail={tail}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .resolveTemplate("tail", tail)
                .request()
                .get();

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
        String url = endPoint + "/containers/json";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if(response.getStatus() != Response.Status.OK.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }

        InputStream r = response.readEntity(InputStream.class);
        ContainersInfoReader reader = new ContainersInfoReader();
        Collection<ContainerInfo> containers = reader.getContainers(r);

        return containers;

    }



}
