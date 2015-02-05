package com.poolingpeople.deployer.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Created by alacambra on 2/5/15.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "",
                query = "SELECT cf from ClusterConfig as cf where cf.serverDomain like :serverDomain")
}
)
public class ClusterConfig {

    public static final String getAllClusters = "com.poolingpeople.deployer.entity.getAllClusters";

    @Id
    Long id;

    Integer portPrefix;
    String neo4jId;
    String wildflyId;
    String appVersion;
    String serverDomain;
    String concretDomain;
    String dbScenario;
    String appBaseName;

    public String getAppBaseName() {
        return appBaseName;
    }

    public void setAppBaseName(String appBaseName) {
        this.appBaseName = appBaseName;
    }

    public String getFullApplicationName(){
        return "rest-" + appVersion;
    }

    public Integer getPortPrefix() {
        return portPrefix;
    }

    public void setPortPrefix(Integer portPrefix) {
        this.portPrefix = portPrefix;
    }

    public String getNeo4jId() {
        return neo4jId;
    }

    public void setNeo4jId(String neo4jId) {
        this.neo4jId = neo4jId;
    }

    public String getWildflyId() {
        return wildflyId;
    }

    public void setWildflyId(String wildflyId) {
        this.wildflyId = wildflyId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }

    public String getConcretDomain() {
        return concretDomain;
    }

    public void setConcretDomain(String concretDomain) {
        this.concretDomain = concretDomain;
    }

    public String getDbScenario() {
        return dbScenario;
    }

    public void setDbScenario(String dbScenario) {
        this.dbScenario = dbScenario;
    }

    public Long getId() {
        return id;
    }
}
