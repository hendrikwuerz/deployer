package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.versions.boundary.VersionsApi;
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
public class DockerPackage {

    @Inject
    VersionsApi versionsApi;

    Logger logger = Logger.getLogger(this.getClass().getName());

    public byte[] prepareDockerBuildTar(){

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

    private void addWar(TarArchiveOutputStream tarArchiveOS){

        InputStream warFileIS = versionsApi.getWarForVersion("rest-0.0.1.war");

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
}
