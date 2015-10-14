package com.poolingpeople.deployer.boundary;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private String appEnvironment = "test";
    private String area;
    private boolean forceDownload; // force download even if cache file was found
    private boolean overwrite; // overwrites existing cluster if 'subdomain' is already used

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

    public String getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(String appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    public Collection<String> getAvailableVersions() {
        Collection<String> availableVersions = facade.loadVersions(area);
        // sort results
        availableVersions = availableVersions.stream().sorted().collect(Collectors.toList());
        // set a default value if nothing is selected
        if(version == null) availableVersions.stream().findAny().ifPresent( element -> version = element);
        return availableVersions;
    }

    public Collection<String> getDbSnapshotsList() {
        return facade.loadDbSnapshots().stream().map(s -> s.split("/")[1]).sorted().collect(Collectors.toList());
    }

    public String deploy() throws IOException {
        facade.deploy(version, subdomain, dbSnapshotName, area, forceDownload, overwrite, appEnvironment);

        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Deployed", "A new cluster has been deployed"));
        return "/faces/console/clusters-list";
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

        return "/deployer/version-select.xhtml";
    }

    public void setArea(String area) {
        this.area = area;
        forceDownload = area.equals("snapshots"); // default select force download for snapshots
    }

    public String getArea() {
        return area;
    }

    public void setForceDownload(boolean forceDownload) {
        this.forceDownload = forceDownload;
    }

    public boolean getForceDownload() {
        return forceDownload;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean getOverwrite() {
        return overwrite;
    }

    /**
     * Send passed bytes as a download to the user
     * @param tarFile
     *          The tar file to be send as a byte array
     */
    private void downloadTar(byte[] tarFile) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseHeader("Content-Type", "application/tar");
        externalContext.setResponseHeader("Content-Length", String.valueOf(tarFile.length));
        externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"some.tar.gz\"");
        try {
            externalContext.getResponseOutputStream().write(tarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        facesContext.responseComplete();
    }

    /**
     * download the docker tar file for the selected version
     */
    public void downloadVersion() throws IOException {
        downloadTar(facade.downloadWar(version, area, appEnvironment, forceDownload));
    }

    /**
     * download the docker tar file for the selected database snapshot
     */
    public void downloadDatabase() throws IOException {
        downloadTar(facade.downloadNeo4J(dbSnapshotName));
    }
}