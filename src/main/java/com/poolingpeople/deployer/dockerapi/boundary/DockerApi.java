package com.poolingpeople.deployer.dockerapi.boundary;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
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
import java.util.logging.Logger;

import static javax.ws.rs.client.Entity.entity;

/**
 * Created by alacambra on 03.02.15.
 */
public class DockerApi implements Serializable{

    @Inject
    DockerEndPointProvider endPointProvider;

    String endPoint = null;

    Logger logger = Logger.getLogger(this.getClass().getName());

    @PostConstruct
    public void init(){
        endPoint = "http://" + endPointProvider.getDockerHost() + ":" + endPointProvider.getPort();
    }

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

    public void deleteImage(String imageName){

        String url = endPoint + "/images/{imageName}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("imageName", imageName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        if(response.getStatus() != Response.Status.OK.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }
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

    public String createContainer(CreateContainerBodyWriter bodyBuilder, String name){
        String url = endPoint + "/containers/create?name={name}";
        Client client = ClientBuilder.newClient();

        String body = bodyBuilder.getObjectBuilder().build().toString();

        logger.fine("creating container with body: " + body);

        Response response = client
                .target(url)
                .resolveTemplate("name", name)
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

    public ContainerNetworkSettingsReader getContainerNetwotkSettings(String containerId){

        String url = endPoint + "/containers/{containerId}/json";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .get();

        if(response.getStatus() != Response.Status.OK.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }

        return new ContainerNetworkSettingsReader(response.readEntity(JsonObject.class));
    }

    public void startContainer(String containerId){
        String url = endPoint + "/containers/{containerId}/start";
        Client client = ClientBuilder.newClient();

        logger.fine("Starting container " + containerId);

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .post(Entity.json(""));

        checkStatusResponseCode(response.getStatus());

    }

    public void stopContainer(String containerId){
        String url = endPoint + "/containers/{containerId}/stop";
        Client client = ClientBuilder.newClient();

        logger.fine("Stoping container " + containerId);

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .post(Entity.json(""));

        checkStatusResponseCode(response.getStatus());

    }

    public void killContainer(String containerId){
        String url = endPoint + "/containers/{containerId}/kill";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .post(Entity.json(""));

        checkStatusResponseCode(response.getStatus());

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

        if(response.getStatus() != Response.Status.OK.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }

        String r = response.readEntity(String.class);
        return r;

    }

    public void removeContainer(String containerId, boolean force){
        String url = endPoint + "/containers/{containerId}?force={force}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .resolveTemplate("force", force)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .delete();

        if(response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()){
            throw new RuntimeException("returned code " + response.getStatus());
        }
    }

    public Collection<ContainerInfo> listContainers(){
        String url = endPoint + "/containers/json?all=1";
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

    private void checkStatusResponseCode(int status){
        if(Response.Status.fromStatusCode(status).getFamily() == Response.Status.Family.CLIENT_ERROR){
            throw new RuntimeException("returned code " + status);
        } else if(Response.Status.fromStatusCode(status).getFamily() == Response.Status.Family.SERVER_ERROR){
            throw new RuntimeException("returned code " + status);
        }else if(Response.Status.fromStatusCode(status).getFamily() == Response.Status.Family.INFORMATIONAL){
            logger.fine("returned code " + status);
        }else if(Response.Status.fromStatusCode(status).getFamily() == Response.Status.Family.REDIRECTION){
            logger.fine("returned code " + status);
        }
        else if(Response.Status.fromStatusCode(status).getFamily() != Response.Status.Family.SUCCESSFUL){
            throw new RuntimeException("returned code " + status);
        }
    }

}
