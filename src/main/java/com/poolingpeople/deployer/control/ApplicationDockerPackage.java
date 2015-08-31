package com.poolingpeople.deployer.control;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class ApplicationDockerPackage extends DockerCluster {

    String appEnvironment;
    byte[] warFileBytes;
    Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected DockerCluster addResources() {
        addWar();
        addFile("Dockerfile-wf", "Dockerfile");
        addFile("standalone.conf", "standalone.conf");
        addFile("standalone.xml", "standalone.xml");
        return this;
    }

    public void addWar(){

        try {
            TarArchiveEntry entry = new TarArchiveEntry(clusterConfig.getFullApplicationName() + ".war");
            entry.setSize(warFileBytes.length);
            tarArchiveOS.putArchiveEntry(entry);
            tarArchiveOS.write(warFileBytes);
            tarArchiveOS.closeArchiveEntry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String replaceClusterBars(String original){
        return original.replace("{NEO_INSTANCE}", clusterConfig.getNeo4jId())
                .replace("{EMAIL_PASSWORD}", System.getenv("EMAIL_PASSWORD")) // this is an env var set on the deployer system
                .replace("{PP_FINAL_NAME}", clusterConfig.getFullApplicationName() + ".war")
                .replace("{PP_APP_ENVIRONMENT}", appEnvironment);
    }

    public void setAppEnvironment(String appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    public void setWarFileBytes(byte[] warFileBytes) {
        this.warFileBytes = warFileBytes;
    }
}
