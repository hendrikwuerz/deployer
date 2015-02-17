package com.poolingpeople.deployer.boundary;

import com.poolingpeople.search.solr.PivotalExporter;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by alacambra on 17.02.15.
 */
@Named
@RequestScoped
public class ExporterController{

    @Inject
    Instance<PivotalExporter> pivotalExporters;

    @Inject
    PivotalExporter pivotalExporter;

    @Inject
    Neo4jAdapterProducer neo4jAdapterProducer;

    String neo4jHost;
    Integer neo4jPort = 80;

    public String getNeo4jHost() {
        return neo4jHost;
    }

    public void setNeo4jHost(String neo4jHost) {
        this.neo4jHost = neo4jHost;
    }

    public Integer getNeo4jPort() {
        return neo4jPort;
    }

    public void setNeo4jPort(Integer neo4jPort) {
        this.neo4jPort = neo4jPort;
    }

    public void export(){
        neo4jAdapterProducer.setHost(neo4jHost);
        neo4jAdapterProducer.setPort(neo4jPort);
        pivotalExporters.get().init();
//        pivotalExporter.init();
    }
}
