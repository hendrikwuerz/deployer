package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.entity.ClusterConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.print.Doc;
import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by alacambra on 03.02.15.
 */
public class ProxyDockerPackage extends DockerPackage {

    @PersistenceContext
    EntityManager em;

    @Override
    protected DockerPackage addResources() {

        List<ClusterConfig> clusterConfigs =
                em.createNamedQuery(ClusterConfig.getAllClusters)
                .setParameter("serverDomain", clusterConfig.getServerDomain())
                .getResultList();

        return this;
    }

    @Override
    String replaceClusterBars(String original) {
        return original.replace("{NEO_INSTANCE}", clusterConfig.getNeo4jId())
                .replace("{PP_FINAL_NAME}", clusterConfig.getFullApplicationName() + ".war");
    }
}
