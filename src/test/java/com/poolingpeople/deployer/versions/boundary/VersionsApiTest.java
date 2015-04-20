package com.poolingpeople.deployer.versions.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import java.io.*;
import java.util.Collection;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class VersionsApiTest {

    VersionsApi cut;

    @Before
    public void setUp(){
        cut = new VersionsApi();
    }

    @Test
    public void testParseVersions() throws Exception {

        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("nexus-snapshots-response.json");
        Collection<String> versions = cut.parseVersions(stream);
        System.out.println(versions);
        assertThat(versions, containsInAnyOrder("0.0.2-SNAPSHOT", "0.0.1-SNAPSHOT", "0.0.0-SNAPSHOT"));

        stream = this.getClass().getClassLoader().getResourceAsStream("nexus-releases-response.json");
        versions = cut.parseVersions(stream);
        System.out.println(versions);
        assertThat(versions, containsInAnyOrder("0.0.1"));
    }

    @Test
    @Ignore
    public void downloadFile() throws IOException, CompressorException, ArchiveException {

        String url = "http://nexus.intern.poolingpeople.com/service/local/repositories/releases/content/com/poolingpeople/rest/0.0.1/rest-0.0.1.war";

        Client client = ClientBuilder.newClient();
        Response response = client
                .target(url)
                .request()
                .header("Authorization", getBasicAuthentication())
                .get();

        InputStream warFileIS = response.readEntity(InputStream.class);
        ByteArrayOutputStream tarByteStream = new ByteArrayOutputStream();
        TarArchiveOutputStream tarArchiveOS = new TarArchiveOutputStream(tarByteStream);

        byte[] bytes = IOUtils.toByteArray(warFileIS);
        TarArchiveEntry entry = new TarArchiveEntry("rest-0.0.1.war");
        entry.setSize(bytes.length);
        tarArchiveOS.putArchiveEntry(entry);
        tarArchiveOS.write(bytes);

        warFileIS.close();
        response.close();

        byte[] tarBytes = tarByteStream.toByteArray();

        ByteArrayOutputStream gzipByteStream = new ByteArrayOutputStream();

        GzipCompressorOutputStream gzippedOut = new GzipCompressorOutputStream(gzipByteStream);
        gzippedOut.write(tarBytes);

        tarArchiveOS.closeArchiveEntry();
        gzippedOut.close();

        File f = new File("/home/alacambra/test.tar.gz");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(gzipByteStream.toByteArray());
        fos.close();


//        client = ClientBuilder.newClient();
//        response = client.target("")
//                .request()
//                .header("Content-Type", "application/java-archive")
//                .post(Entity.entity(gzippedOut, "application/tar"));


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

    private Byte[] inputStreamToByteArray(InputStream stream){
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//
//        int nRead;
//        byte[] data = new byte[16384];
//
//        while ((nRead = stream.read(data, 0, data.length)) != -1) {
//            buffer.write(data, 0, nRead);
//        }
//
//        buffer.flush();
//
//        return buffer.toByteArray();

        return null;
    }
}































