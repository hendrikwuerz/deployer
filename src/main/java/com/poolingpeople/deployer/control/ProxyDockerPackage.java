package com.poolingpeople.deployer.control;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.inject.Inject;
import java.io.*;
import java.util.logging.Logger;

/**
 * Created by alacambra on 03.02.15.
 */
public class ProxyDockerPackage {

    boolean materiliazeFile = false;
    String materializationPath = "";

    Logger logger = Logger.getLogger(this.getClass().getName());

    public byte[] prepareDockerApplicationBuildTar(){

        ByteArrayOutputStream tarByteStream = new ByteArrayOutputStream();
        TarArchiveOutputStream tarArchiveOS = new TarArchiveOutputStream(tarByteStream);
        addWar(tarArchiveOS);

        /*
         * Compress the tar file
         */
        byte[] tarBytes = tarByteStream.toByteArray();
        ByteArrayOutputStream gzipByteStream = new ByteArrayOutputStream();
        GzipCompressorOutputStream gzippedOut;

        try {

            gzippedOut = new GzipCompressorOutputStream(gzipByteStream);
            gzippedOut.write(tarBytes);
            gzippedOut.close();
            tarArchiveOS.closeArchiveEntry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] bytes =  gzipByteStream.toByteArray();
        logger.finer("Final tar has " + bytes.length + " bytes");
        return gzipByteStream.toByteArray();
    }

    public byte[] prepareDockerProxyBuildTar(){

        ByteArrayOutputStream tarByteStream = new ByteArrayOutputStream();
        TarArchiveOutputStream tarArchiveOS = new TarArchiveOutputStream(tarByteStream);
        addWar(tarArchiveOS);

        /*
         * Compress the tar file
         */
        byte[] tarBytes = tarByteStream.toByteArray();
        ByteArrayOutputStream gzipByteStream = new ByteArrayOutputStream();
        GzipCompressorOutputStream gzippedOut;

        try {

            gzippedOut = new GzipCompressorOutputStream(gzipByteStream);
            gzippedOut.write(tarBytes);
            gzippedOut.close();
            tarArchiveOS.closeArchiveEntry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] bytes =  gzipByteStream.toByteArray();
        logger.finer("Final tar has " + bytes.length + " bytes");
        return gzipByteStream.toByteArray();
    }

    private void materializeTarFile(ByteArrayOutputStream gzipByteStream){

        File f = new File("/home/alacambra/test.tar.gz");
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(f);
            fos.write(gzipByteStream.toByteArray());
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void nginxDocker(){
        InputStream stream = loadResource("Dockerfile-nginx");

    }

    public void nginxDomainConfFile(){
        InputStream stream = loadResource("site.conf");

    }

    public void neo4jDockerfile(){
        InputStream stream = loadResource("Dockerfile-wf");

    }

    public void wfDockerFile(){
        InputStream stream = loadResource("Dockerfile-neo4j");

    }

    private void addWar(TarArchiveOutputStream tarArchiveOS){

        InputStream warFileIS = null;

        try {

            byte[] bytes = IOUtils.toByteArray(warFileIS);
            TarArchiveEntry entry = new TarArchiveEntry("rest-0.0.1.war");
            entry.setSize(bytes.length);
            tarArchiveOS.putArchiveEntry(entry);
            tarArchiveOS.write(bytes);

        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    public void setMaterializationPath(String materializationPath) {
        this.materializationPath = materializationPath;
    }


    private InputStream loadResource(String resourceName){
        return this.getClass().getClassLoader().getResourceAsStream(resourceName);
    }
}
