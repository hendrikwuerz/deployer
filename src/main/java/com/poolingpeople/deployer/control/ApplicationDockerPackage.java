package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.application.boundary.VersionsApi;
import com.poolingpeople.deployer.entity.ClusterConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class ApplicationDockerPackage extends DockerCluster {

    InputStream warFileIS;

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
            byte[] bytes = new ByteArrayOutputStream().toByteArray();
            TarArchiveEntry entry = new TarArchiveEntry(clusterConfig.getFullApplicationName() + ".war");
            entry.setSize(bytes.length);
            tarArchiveOS.putArchiveEntry(entry);
            tarArchiveOS.write(bytes);
            warFileIS.close();
            tarArchiveOS.closeArchiveEntry();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String replaceClusterBars(String original){
        return original.replace("{NEO_INSTANCE}", clusterConfig.getNeo4jId())
                .replace("{PP_FINAL_NAME}", clusterConfig.getFullApplicationName() + ".war");
    }

    public void setWarFileIS(InputStream warFileIS) {
        this.warFileIS = warFileIS;
    }
}
