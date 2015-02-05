package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.entity.ClusterConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by alacambra on 03.02.15.
 */
public abstract class DockerPackage {

    ClusterConfig clusterConfig;
    TarArchiveOutputStream tarArchiveOS;

    byte[] tarBytes;

    Logger logger = Logger.getLogger(this.getClass().getName());

    public byte[] getBytes(){
        return tarBytes;
    }

    protected abstract DockerPackage addResources();

    public DockerPackage prepareTarStream(){

        ByteArrayOutputStream tarByteStream = new ByteArrayOutputStream();
        tarArchiveOS = new TarArchiveOutputStream(tarByteStream, "UTF-8");
        tarArchiveOS.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        addResources();

        tarBytes = tarByteStream.toByteArray();
        compressTarStream();
        return this;
    }

    protected void compressTarStream(){

        ByteArrayOutputStream gzipByteStream = new ByteArrayOutputStream();
        GzipCompressorOutputStream gzippedOut;

        try {

            gzippedOut = new GzipCompressorOutputStream(gzipByteStream);
            gzippedOut.write(tarBytes);
            gzippedOut.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tarBytes = gzipByteStream.toByteArray();
        logger.finer("Final tar has " + tarBytes.length + " bytes");
    }

    abstract String replaceClusterBars(String original);

    public void addFile(String resourceName, String fileName){

        InputStream stream = loadResource(resourceName);

        InputStreamReader streamReader = new InputStreamReader(stream, Charset.forName("UTF-8"));
        String targetString = replaceClusterBars(readerToString(streamReader));

        try {

            TarArchiveEntry entry = new TarArchiveEntry(fileName);
            entry.setSize(targetString.length());
            tarArchiveOS.putArchiveEntry(entry);
            tarArchiveOS.write(targetString.getBytes(Charset.forName("UTF-8")));
            tarArchiveOS.closeArchiveEntry();
            stream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void materializeTarFile(String path){

        File f = new File(path);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(f);
            fos.write(tarBytes);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TarArchiveOutputStream getTarArchiveOS() {
        return tarArchiveOS;
    }

    private InputStream loadResource(String resourceName){
        return this.getClass().getClassLoader().getResourceAsStream(resourceName);
    }

    private String readerToString(Reader initialReader){

        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;

        try {
            while ((numCharsRead = initialReader.read(arr, 0, arr.length)) != -1) {
                buffer.append(arr, 0, numCharsRead);
            }

            initialReader.close();
            String targetString = buffer.toString();
            return targetString;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }
    public void setClusterConfig(ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }
}
