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
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static javax.ws.rs.client.Entity.entity;

/**
 * Created by alacambra on 03.02.15.
 */
public class DockerApi implements Serializable{

    @Inject
    DockerEndPoint endPoint;

//    public void setEndPoint(@Observes DockerEndPoint endPoint) {
//        this.endPoint = endPoint;
//    }

    Logger logger = Logger.getLogger(this.getClass().getName());

    public DockerApi(DockerEndPoint endPoint) {
        this.endPoint = endPoint;
        init();
    }

    public DockerApi() {
    }

    @PostConstruct
    public void init(){

    }

    public String getDockerInfo(){
        return "";
    }

    public String buildImage(String imageName, InputStream tarStream){
        String url = endPoint.getURI() + "/build?t={imageName}";

        Client client = ClientBuilder.newClient();
        Response response = client
                .target(url)
                .resolveTemplate("imageName", imageName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(entity(tarStream, "application/tar"), Response.class);

        checkStatusResponseCode(response.getStatus());
        String r = response.readEntity(String.class);

        if (r.contains("errorDetail")){
            throw new RuntimeException(r);
        }

        logger.info(r);
        return r;
    }

    public void deleteImage(String imageName){

        String url = endPoint.getURI() + "/images/{imageName}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("imageName", imageName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        checkStatusResponseCode(response.getStatus());

    }

    public String listImage(){

        String url = endPoint.getURI() + "/images/json?all=0";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .get();

        checkStatusResponseCode(response.getStatus());
        String r = response.readEntity(String.class);
        return r;

    }

    public String createContainer(CreateContainerBodyWriter bodyBuilder, String name){
        String url = endPoint.getURI() + "/containers/create?name={name}";
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

        checkStatusResponseCode(response.getStatus());

        JsonObject object = Json.createReader(r).readObject();

        return ((JsonString)object.get("Id")).getString();

    }

    public ContainerNetworkSettingsReader getContainerNetwotkSettings(String containerId){

        String url = endPoint.getURI() + "/containers/{containerId}/json";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .header("Content-Type", "application/json")
                .get();

        checkStatusResponseCode(response.getStatus());

        return new ContainerNetworkSettingsReader(response.readEntity(JsonObject.class));
    }

    public void startContainer(String containerId){
        String url = endPoint.getURI() + "/containers/{containerId}/start";
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
        String url = endPoint.getURI() + "/containers/{containerId}/stop";
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
        String url = endPoint.getURI() + "/containers/{containerId}/kill";
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

        String url = endPoint.getURI() + "/containers/{containerId}/logs?stderr=1&stdout=1&timestamps=1&follow=0&tail={tail}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .resolveTemplate("tail", tail)
                .request()
                .get();

        checkStatusResponseCode(response.getStatus());

        String r = response.readEntity(String.class);
        return r;

    }

    public void removeContainer(String containerId, boolean force){
        String url = endPoint.getURI() + "/containers/{containerId}?force={force}";
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .resolveTemplate("force", force)
                .request()
                .accept(MediaType.TEXT_PLAIN)
                .delete();

        checkStatusResponseCode(response.getStatus());

    }

    /**
     * list containers for current endPoint
     * @return
     *          A collection with all containers
     */
    public Collection<ContainerInfo> listContainers() {
        return listContainers(endPoint.getURI());
    }


    /**
     * list containers for passed endPoint URI
     * @param url
     *          The url of the host to get the containers for
     *          Example: http://localhost:5555
     * @return
     *          A collection with all containers
     */
    public Collection<ContainerInfo> listContainers(String url) {
        url = url + "/containers/json?all=1";
        Client client = ClientBuilder.newClient();


        Future<Response> future = client
                .target(url)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .buildGet()
                .submit();

        try {
            Response response = future.get(1, TimeUnit.SECONDS);

            checkStatusResponseCode(response.getStatus());

            InputStream r = response.readEntity(InputStream.class);
            ContainersInfoReader reader = new ContainersInfoReader();
            Collection<ContainerInfo> containers = reader.getContainers(r);
            //logger.fine("FINISHED REQUEST with " + response.getStatus());

            return containers;
        } catch (InterruptedException | ExecutionException e) {
            logger.warning("Exception for " + url);
            e.printStackTrace();
        } catch (TimeoutException e) {
            logger.warning(url + " seams to be not available");
        }
        return null;
    }

    public InputStream copyFiles(String containerId, String filePath){
        return copyFiles(endPoint, containerId, filePath);
    }
    public InputStream copyFiles(DockerEndPoint endPoint, String containerId, String filePath){
        String url = endPoint.getURI() + "/containers/{containerId}/copy";
        Client client = ClientBuilder.newClient();

        String json = Json.createObjectBuilder().add("Resource", filePath).build().toString();

        Response response = client
                .target(url)
                .resolveTemplate("containerId", containerId)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(json));

        checkStatusResponseCode(response.getStatus());

        InputStream stream = response.readEntity(InputStream.class);
        return stream;
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
        }else if(Response.Status.fromStatusCode(status).getFamily() != Response.Status.Family.SUCCESSFUL){
            throw new RuntimeException("returned code " + status);
        }
    }

}
