package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class Neo4jDockerPackage extends DockerPackage {

    @Override
    protected DockerPackage addResources() {
        addFile("Dockerfile-neo4j", "Dockerfile");
        return this;
    }

    @Override
    String replaceClusterBars(String original) {
        return original;
    }

}
