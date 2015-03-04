package com.poolingpeople.deployer.boundary;

import javax.enterprise.context.RequestScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by alacambra on 2/6/15.
 */
@Named
@RequestScoped
public class DeployerController {

    @Inject
    DeployerFacade facade;

    private String subdomain;
    private String version;
    private String dbSnapshotName;

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
        return facade.loadVersions();
    }

    public Collection<String> getDbSnapshotsList() {
        return facade.loadDbSnapshots();
    }

    public void deploy(){
        facade.deploy(version, subdomain);
    }

    public String getDbSnapshotName() {
        return dbSnapshotName;
    }

    public void setDbSnapshotName(String dbSnapshotName) {
        this.dbSnapshotName = dbSnapshotName;
    }
}
