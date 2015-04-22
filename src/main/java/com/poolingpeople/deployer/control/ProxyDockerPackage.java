package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.entity.ClusterConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

/**
 * Created by alacambra on 03.02.15.
 */
public class ProxyDockerPackage extends DockerCluster {

    private Collection<ClusterConfig> configs;

    private DomainConfigInfo currentDomainConfigInfo;

    private class DomainConfigInfo{
        String domain;
        String target;
        String port;
        String gateway;
    }

    @Override
    protected DockerCluster addResources() {

        for(ClusterConfig config : configs){
            addNeo4jFile(config);
            addWebappFile(config);
            addWfConsoleFile(config);
            addTalkWSConsoleFile(config);
        }

        addFile("Dockerfile-nginx", "Dockerfile");
        addFile("nginx.conf", "nginx.conf");
        addFile("default.conf", "default.conf");
        addFile("404.html", "404.html");
        return this;
    }

    public ProxyDockerPackage setClusterConfigs(Collection<ClusterConfig> clusterConfigs) {
        configs = clusterConfigs;
        return this;
    }

    void addNeo4jFile(ClusterConfig config){
        currentDomainConfigInfo = new DomainConfigInfo();
        currentDomainConfigInfo.domain = "neo4j." + config.getConcretDomain() + "." + config.getServerDomain();
        currentDomainConfigInfo.port = config.getPortPrefix() + config.getNeo4jPort();
        currentDomainConfigInfo.target = config.getNeo4jId();
        currentDomainConfigInfo.gateway = config.getGateway();

        addFile("site.conf", "conf/" + currentDomainConfigInfo.domain + ".conf");
    }

    void addWfConsoleFile(ClusterConfig config){
        currentDomainConfigInfo = new DomainConfigInfo();
        currentDomainConfigInfo.domain = "admin." + config.getConcretDomain() + "." + config.getServerDomain();
        currentDomainConfigInfo.port = config.getPortPrefix() + config.getWfAdminPort();
        currentDomainConfigInfo.target = config.getWildflyId();
        currentDomainConfigInfo.gateway = config.getGateway();

        addFile("site.conf", "conf/" + currentDomainConfigInfo.domain + ".conf");
    }

    void addTalkWSConsoleFile(ClusterConfig config){
//        currentDomainConfigInfo = new DomainConfigInfo();
//        currentDomainConfigInfo.domain = "tws." + config.getConcretDomain() + "." + config.getServerDomain();
//        currentDomainConfigInfo.port = config.getPortPrefix() + config.getWfAdminPort();
//        currentDomainConfigInfo.target = config.getWildflyId();
//        currentDomainConfigInfo.gateway = config.getGateway();
//
//        addFile("talk-ws.conf", "conf/" + currentDomainConfigInfo.domain + ".conf");
    }

    void addWebappFile(ClusterConfig config){
        currentDomainConfigInfo = new DomainConfigInfo();
        currentDomainConfigInfo.domain = config.getConcretDomain() + "." + config.getServerDomain();
        currentDomainConfigInfo.port = config.getPortPrefix() + config.getWfPort();
        currentDomainConfigInfo.target = config.getWildflyId();
        currentDomainConfigInfo.gateway = config.getGateway();

        addFile("site.conf", "conf/" + currentDomainConfigInfo.domain + ".conf");
    }


    @Override
    String replaceClusterBars(String original) {

        if ( currentDomainConfigInfo == null )
            return original;

        return original.replace("{DOMAIN}", currentDomainConfigInfo.domain)
                .replace("{TARGET}", currentDomainConfigInfo.gateway)
                .replace("{PORT}", String.valueOf(currentDomainConfigInfo.port));
    }
}
