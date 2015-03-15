package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.scenario.boundary.DbSnapshot;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;

public class Neo4jDockerPackage extends DockerCluster {

    DbSnapshot dbSnapshot;

    @Override
    protected DockerCluster addResources() {
        String dockerFileToUse = dbSnapshot == null ? "" : "-withdb";

        addFile("Dockerfile-neo4j" +dockerFileToUse, "Dockerfile");
        addDbSnapshot();
        return this;
    }

    public void addDbSnapshot() {

        try {

            InputStream stream = dbSnapshot.fetchSnapshot();

            byte[] bytes = IOUtils.toByteArray(stream);
            stream.close();

            TarArchiveEntry entry = new TarArchiveEntry(dbSnapshot.getSnapshotName());
            entry.setSize(bytes.length);
            tarArchiveOS.putArchiveEntry(entry);
            tarArchiveOS.write(bytes);
            tarArchiveOS.closeArchiveEntry();

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
