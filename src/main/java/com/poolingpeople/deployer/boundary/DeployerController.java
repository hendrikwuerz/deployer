package com.poolingpeople.deployer.boundary;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alacambra on 2/6/15.
 */
@Named
@SessionScoped
public class DeployerController implements Serializable {

    @Inject
    DeployerFacade facade;

    private String subdomain;
    private String version;
    private String dbSnapshotName;
    private String area;

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public Collection<String> getAvailableVersions() {
        return facade.loadVersions(area);
    }

    public Collection<String> getDbSnapshotsList() {
        return facade.loadDbSnapshots().stream().map(s -> s.split("/")[1]).collect(Collectors.toList());
    }

    public void deploy(){
        facade.deploy(version, subdomain, dbSnapshotName, area);
    }

    public String getDbSnapshotName() {
        return dbSnapshotName;
    }

    public void setDbSnapshotName(String dbSnapshotName) {
        this.dbSnapshotName = dbSnapshotName;
    }

    public String selectArea() {
        Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        setArea(params.get("area"));
        return "version-select";
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getArea() {
        return area;
    }
}