package com.poolingpeople.deployer.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Optional;

/**
 * Created by alacambra on 2/5/15.
 */
public class ClusterConfig {

    public static final String clusterSeparator = "--";

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


    String serverIp;

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

    /**
     *
     */
    String gateway = "";

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
        return appBaseName + clusterSeparator + appVersion;
    }

    public String getPortPrefix() {
        return portPrefix;
    }

    public ClusterConfig setPortPrefix(String portPrefix) {
        this.portPrefix = portPrefix;
        return this;
    }

    public String getNeo4jId() {
        return getPortPrefix() + clusterSeparator + "neo4j" + clusterSeparator + getDbScenario() + clusterSeparator + getConcretDomain() + clusterSeparator + getServerDomain();
    }

    public ClusterConfig setNeo4jId(String neo4jId) {
        this.neo4jId = neo4jId;
        return this;
    }

    public String getWildflyId() {
        return getPortPrefix() + clusterSeparator + "wf" + clusterSeparator
                + getAppVersion() + clusterSeparator + getConcretDomain() + clusterSeparator + getServerDomain();
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

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerIp() {
        return serverIp;
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
        return Optional.ofNullable(dbScenario).orElse("none");
    }

    public ClusterConfig setDbScenario(String dbScenario) {
        this.dbScenario = dbScenario;
        return this;
    }

    public String getId() {

        String id = "";

        Optional.ofNullable(id += getConcretDomain() + clusterSeparator).orElse("none" + clusterSeparator);
        Optional.ofNullable(id += getServerDomain()).orElse("no_server");

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

    /**
     * On systems with other (no-pp) docker container, check is the passed name
     * matches the conventions.
     * Used before passing container to loadFromContainerName(String)
     * @param containerName
     *          The name to be checked
     * @return
     *          true if this is a pp-container, false otherwise
     */
    public static boolean isLoadableContainerName(String containerName) {
        String[] conf = containerName.split(clusterSeparator);
        return conf.length == 5;
    }

    public ClusterConfig loadFromContainerName(String containerName){

        String[] conf = containerName.split(clusterSeparator);

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
        setGateway(conf[4]);

        return this;
    }

    public ClusterConfig mergeWith(ClusterConfig clusterConfig){

        if(!equals(clusterConfig)) return this;

        if(getAppVersion() == null){
            setAppVersion(clusterConfig.getAppVersion());
        } else {
            setDbScenario(clusterConfig.getDbScenario());
        }

        return this;
    }

    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof ClusterConfig)){
            return false;
        }

        /*
         * cluster values are not initialized
         */
        if(concretDomain == null || serverDomain == null){
            return super.equals(obj);
        }

        return getId().equals(((ClusterConfig) obj).getId());

    }

    @Override
    public String toString() {
        return getId();
    }

    public String getGateway() {
        return gateway;
    }

    public ClusterConfig setGateway(String gateway) {
        this.gateway = gateway;
        return this;
    }
}
