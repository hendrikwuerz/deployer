package com.poolingpeople.deployer.versions.boundary;


import javax.inject.Inject;
import javax.json.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 03.02.15.
 */
public class VersionsApi {


    public Collection<String> loadVersions(){

        String endpoint = "http://nexus.poolingpeople.com/service/local/repositories/snapshots/content/com/poolingpeople/rest/";

        Client client = ClientBuilder.newClient();
        Response response = client
                .target(endpoint)
                .request()
                .header("Authorization", getBasicAuthentication())
                .header("Accept", "application/json; charset=UTF-8")
                .header("Content-Type", "application/json")
                .get();

        InputStream stream = response.readEntity(InputStream.class);
        Collection<String> versions = parseVersions(stream);
        response.close();
        return versions;

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
}
