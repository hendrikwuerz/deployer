package com.poolingpeople.deployer.application.boundary;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.conn.EofSensorInputStream;

import org.apache.commons.compress.utils.IOUtils;

import javax.json.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
     * @param forceDownload
     *          download war again even if it is cached
     * @return
     *          The requested war file
     */

    public byte[] getWarForVersion(String version, String area, boolean forceDownload) {

        InputStream warFileIS;

        File cacheFile = getCacheFile(version, area);

        // use cache only if file exists, and no force is set
        // only releases are cached because snapshots can be changed
        if(cacheFile.exists() && !forceDownload) { // no need to download again -> use cache
            try {

                warFileIS = new FileInputStream(cacheFile);
                logger.fine("Use cached file " + cacheFile.getAbsolutePath());
                return getBytesFromStream(warFileIS);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return downloadWarForVersion(version, area);
            }

        } else { // no cache found
            return downloadWarForVersion(version, area);
        }
    }

    /**
     * download the war for the passed version and area.
     * The result will also be stored in a cache file if downloading a release.
     * @param version
     *          The wished
     * @param area
     *          "snapshots" or "releases"
     * @return
     *          The InputStream of the war
     */
    private byte[] downloadWarForVersion(String version, String area) {

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


//        logger.fine("getWarForVersion: input stream read: " + String.valueOf(warFileIS.available()));

        // cache result
        try {
            File cacheFile = getCacheFile(version, area);
            FileOutputStream stream = new FileOutputStream(cacheFile);
            IOUtils.copy(warFileIS, stream);
            stream.close();
            logger.fine("Save war for caching in " + cacheFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return getBytesFromStream(warFileIS);

    }

    private byte[] getBytesFromStream(InputStream stream){

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int b;
            while ((b = stream.read()) != -1){
                outputStream.write(b);
            }

            byte[] bytes = outputStream.toByteArray();
            return bytes;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get the file object to cache the downloaded war
     * @param version
     *          The version which should be stored
     * @param area
     *          "snapshots" or "releases"
     * @return
     *          The file where the data is stored
     */
    private File getCacheFile(String version, String area) {
        return new File("cache/cache-" + area + "-version-" + version + ".war");
    }

    /**
     * list all cached files
     * @return
     *          A collection of all cached files
     */
    public Collection<File> getCachedFiles() {
        try {
            Collection<File> files = new ArrayList<>();

            // Check folder to exists
            File folder = new File("cache");
            if(!folder.exists()) {
                folder.mkdir();
            }

            // find files
            Files.walk(Paths.get("cache")).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    files.add(filePath.toFile());
                }
            });
            return files;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
