package com.poolingpeople.deployer.control;

import com.amazonaws.services.s3.model.S3Object;
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

        S3Object s3Object = dbSnapshot.fetchSnapshot();

        long contentLength = s3Object.getObjectMetadata().getContentLength();
        InputStream stream = s3Object.getObjectContent();

        try {

            // init a new entry in the tar archive
            TarArchiveEntry entry = new TarArchiveEntry(dbSnapshot.getSnapshotName());
            entry.setSize(contentLength);
            tarArchiveOS.putArchiveEntry(entry);

            IOUtils.copy(stream, tarArchiveOS, 8 * 1024);

            // entry is written
            tarArchiveOS.closeArchiveEntry();
            stream.close();

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
