package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.entity.ClusterConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Created by alacambra on 03.02.15.
 */
public abstract class DockerCluster {

    ClusterConfig clusterConfig;
    TarArchiveOutputStream tarArchiveOS;

    InputStream tarStream;
    File tarFile;
    File compressedFile;

    Logger logger = Logger.getLogger(this.getClass().getName());

    public InputStream getTarStream() {
        return tarStream;
    }

    public byte[] getBytes() {
        //return tarBytes;
        try {
            byte[] data = IOUtils.toByteArray(new FileInputStream(compressedFile));
            cleanTempFiles();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        cleanTempFiles();
        return new byte[0];
    }

    protected abstract DockerCluster addResources();

    public DockerCluster prepareTarStream() throws IOException {

        tarFile = File.createTempFile("deployment_tar", ".tar");
        FileOutputStream tarByteStream = new FileOutputStream(tarFile);
        tarArchiveOS = new TarArchiveOutputStream(tarByteStream, "UTF-8");
        tarArchiveOS.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        addResources();

        compressTarStream();

//        materializeTarFile("/home/alacambra/some.tar.gz");
        return this;
    }

    protected void compressTarStream() throws IOException {

        compressedFile = File.createTempFile("compressed_tar", ".tar.gz");
        FileOutputStream gzipFileStream = new FileOutputStream(compressedFile);
        GzipCompressorOutputStream gzippedOut = new GzipCompressorOutputStream(gzipFileStream);
        InputStream stream = new FileInputStream(tarFile);

        try {

            IOUtils.copy(stream, gzippedOut, 8 * 1024);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            stream.close();
            gzippedOut.close();
        }

        tarStream = new FileInputStream(compressedFile);
        logger.finer("Final tar has " + compressedFile.length() + " bytes");
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

        compressedFile.renameTo(new File(path));

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

    public void cleanTempFiles() {
        if(tarFile != null && tarFile.exists()) tarFile.delete();
        if(compressedFile != null && compressedFile.exists()) compressedFile.delete();
    }

}
