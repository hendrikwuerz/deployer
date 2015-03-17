package com.poolingpeople.deployer.application.boundary;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.conn.EofSensorInputStream;

import javax.json.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class VersionsApi {

    Logger logger = Logger.getLogger(getClass().getName());

    /**
     * @param area
     *          "snapshots" or "releases"
     * @return
     *          A collection of all available versions
     */
    public Collection<String> loadVersions(String area){

        String endpoint = "http://nexus.poolingpeople.com/service/local/repositories/" + area + "/content/com/poolingpeople/rest/";

        Response response = fetchVersionsFromNexus(endpoint);
        InputStream stream = response.readEntity(InputStream.class);
        Collection<String> versions = parseVersions(stream);
        response.close();
        return versions;

    }

    /**
     *
     * @param version
     *          The wished version
     * @param area
     *          "snapshots" or "releases"
     * @return
     *          The requested war file
     */
    synchronized public byte[] getWarForVersion(String version, String area){

        String url =
                "http://nexus.poolingpeople.com/service/local/repositories/" +
                "{area}/content/com/poolingpeople/rest/{version}/rest-{version}.war";

        // TODO: modify url ???
        if(area.equals("snapshots")) {
            url = "http://nexus.poolingpeople.com/service/local/repositories/" +
                    "snapshots/content/com/poolingpeople/rest/0.0.0-SNAPSHOT/rest-0.0.0-20141202.110626-1.war";
            url = "http://nexus.poolingpeople.com/service/local/repositories/" +
                    "snapshots/content/com/poolingpeople/rest/0.0.2-SNAPSHOT/rest-0.0.2-20150121.091129-1.war";
        }

        System.out.println(url);
        Client client = ClientBuilder.newClient();
        Invocation.Builder req =  client
                .target(url)
                .resolveTemplate("version", version)
                .resolveTemplate("area", area)
                .request();

        Response response =
                req.header("Authorization", getBasicAuthentication())
                .get();

        checkStatusResponseCode(response.getStatus());
        InputStream warFileIS = response.readEntity(InputStream.class);

        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int b;
            while ((b = warFileIS.read()) != -1){
                outputStream.write(b);
            }

            byte[] bytes = outputStream.toByteArray();
            return bytes;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private Response fetchVersionsFromNexus(String url){
        Client client = ClientBuilder.newClient();
        Response response = client
                .target(url)
                .request()
                .header("Authorization", getBasicAuthentication())
                .header("Accept", "application/json; charset=UTF-8")
                .header("Content-Type", "application/json")
                .get();

        checkStatusResponseCode(response.getStatus());
        return response;
    }


    public Collection<String> parseVersions(String jsonResponse){
        return parseVersions(new ByteArrayInputStream(jsonResponse.getBytes()));
    }

    public Collection<String> parseVersions(InputStream jsonResponse){

        JsonObject object = Json.createReader(new InputStreamReader(jsonResponse)).readObject();
        JsonArray items = (JsonArray) object.get("data");

        Collection<String> versions = items.stream()
                .map(s -> ((JsonString)(((JsonObject)s).get("text"))).getString())
                .filter(s -> !s.endsWith(".sha1"))
                .filter(s -> !s.endsWith(".xml"))
                .filter(s -> !s.endsWith(".md5"))
                .collect(Collectors.toSet());
        return versions;
    }

    private String getBasicAuthentication() {
        String user = "deployer";
        String password = "test1234";
        String token = user + ":" + password;
        try {
            return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Cannot encode with UTF-8", ex);
        }
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
