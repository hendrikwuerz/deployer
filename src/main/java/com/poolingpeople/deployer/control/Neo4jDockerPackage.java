package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.scenario.boundary.DbSnapshot;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;

public class Neo4jDockerPackage extends DockerCluster {

    DbSnapshot dbSnapshot;

    @Override
    protected DockerCluster addResources() {
        String dockerFileToUse = dbSnapshot == null ? "" : "-withdb";

        addFile("Dockerfile-neo4j" +dockerFileToUse, "Dockerfile");

        if(dbSnapshot != null)
            addDbSnapshot();
        return this;
    }

    public void addDbSnapshot() {

        try {

            InputStream stream = dbSnapshot.fetchSnapshot();

            // copy stream to tmp file
            // this is needed to get the file size
            File tmpFile = File.createTempFile("neo4j_deployment_snapshot", ".db");
            OutputStream outputStream = new FileOutputStream(tmpFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            stream.close();

            // init a new entry in the tar archive
            TarArchiveEntry entry = new TarArchiveEntry(dbSnapshot.getSnapshotName());
            entry.setSize(tmpFile.length());
            tarArchiveOS.putArchiveEntry(entry);

            // write the temp file content to the tar archive
            stream = new FileInputStream(tmpFile);
            buffer = new byte[1024];
            while ((bytesRead = stream.read(buffer)) != -1) {
                tarArchiveOS.write(buffer, 0, bytesRead);
            }

            // entry is written
            tarArchiveOS.closeArchiveEntry();
            tmpFile.delete();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    String replaceClusterBars(String original) {
        return dbSnapshot == null ? original : original.replace("{DB-SNAPSHOT-NAME}",dbSnapshot.getSnapshotName());
    }

    public Neo4jDockerPackage setDbSnapshot(DbSnapshot dbSnapshot) {
        this.dbSnapshot = dbSnapshot;
        return this;
    }
}
