package com.poolingpeople.deployer.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * Created by alacambra on 2/5/15.
 */
//@Entity
//@NamedQueries({
//        @NamedQuery(name = "",
//                query = "SELECT cf from ClusterConfig as cf where cf.serverDomain like :serverDomain")
//}
//)
public class ClusterConfig {

    public static final String getAllClusters = "com.poolingpeople.deployer.entity.getAllClusters";

    @Id
    Long id;

    /**
     * the first number from the port. Each cluster will be identified by this number.
     */
    String portPrefix;

    /**
     * neo4j instance name or dockerId (tbd.)
     */
    String neo4jId;

    /**
     * port used for neo4j. Without cluster prefix
     */
    String neo4jPort = "7474";

    /**
     * wf instance name or dockerId (tbd.)
     */
    String wildflyId;

    /**
     * port used for wf. Without cluster prefix
     */
    String wfPort = "8080";

    /**
     * port used for wf console. Without cluster prefix
     */
    String wfAdminPort = "9990";

    /**
     * version of the app (i.e. 0.0.1)
     */
    String appVersion;

    /**
     * registered cname server domain. It identifies physical machine or aws instance
     */
    String serverDomain;

    /**
     * subdomain given by a user to identify its deployment.
     */
    String concretDomain;

    /**
     * name of the scenario loaded in the db
     */
    String dbScenario;

    /**
     * name of the app without version (i.e. rest)
     */
    String appBaseName;

    public String getAppBaseName() {
        return appBaseName;
    }

    public ClusterConfig setAppBaseName(String appBaseName) {
        this.appBaseName = appBaseName;
        return this;
    }

    /**
     * builds the complete application name in the form appBaseName + - + version (i.e. rest-0.0.1)
     * @return
     */
    public String getFullApplicationName(){
        return appBaseName + "-" + appVersion;
    }

    public String getPortPrefix() {
        return portPrefix;
    }

    public ClusterConfig setPortPrefix(String portPrefix) {
        this.portPrefix = portPrefix;
        return this;
    }

    public String getNeo4jId() {
        return neo4jId;
    }

    public ClusterConfig setNeo4jId(String neo4jId) {
        this.neo4jId = neo4jId;
        return this;
    }

    public String getWildflyId() {
        return wildflyId;
    }

    public ClusterConfig setWildflyId(String wildflyId) {
        this.wildflyId = wildflyId;
        return this;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public ClusterConfig setAppVersion(String appVersion) {
        this.appVersion = appVersion;
        return this;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public ClusterConfig setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
        return this;
    }

    public String getConcretDomain() {
        return concretDomain;
    }

    public ClusterConfig setConcretDomain(String concretDomain) {
        this.concretDomain = concretDomain;
        return this;
    }

    public String getDbScenario() {
        return dbScenario;
    }

    public ClusterConfig setDbScenario(String dbScenario) {
        this.dbScenario = dbScenario;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getNeo4jPort() {
        return neo4jPort;
    }

    public ClusterConfig setNeo4jPort(String neo4jPort) {
        this.neo4jPort = neo4jPort;
        return this;
    }

    public String getWfPort() {
        return wfPort;
    }

    public ClusterConfig setWfPort(String wfPort) {
        this.wfPort = wfPort;
        return this;
    }

    public String getWfAdminPort() {
        return wfAdminPort;
    }

    public ClusterConfig setWfAdminPort(String wfAdminPort) {
        this.wfAdminPort = wfAdminPort;
        return this;
    }

    public void loadFromContainerName(String containerName){
        String[] conf = containerName.split("-");

        if(conf.length != 5){
            throw new RuntimeException("Invalid format for " + containerName);
        }

        setPortPrefix(conf[0]);

        if(conf[1].equals("wf")){
            setAppVersion(conf[2]);
        }else if(conf[1].equals("neo4j")){
            setDbScenario(conf[2]);
        }else{
            throw new RuntimeException("Invalid node name " + conf[1]);
        }

        setConcretDomain(conf[3]);
        setServerDomain(conf[4]);
    }

    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof ClusterConfig)){
            return false;
        }

        ClusterConfig ccfg = (ClusterConfig) obj;
        return concretDomain.equals(ccfg.getConcretDomain()) && serverDomain.equals(ccfg.getServerDomain());

    }
}
