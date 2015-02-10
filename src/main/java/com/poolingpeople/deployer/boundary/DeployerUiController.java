package com.poolingpeople.deployer.boundary;

import javax.enterprise.context.RequestScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by alacambra on 2/6/15.
 */
@Named
@RequestScoped
public class DeployerUiController{

    @Inject
    DeployerFacade facade;

    private String subdomain;
    private String version;
    private String imageName;

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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void deploy(){
        imageName = Optional.ofNullable(imageName).orElse(UUID.randomUUID().toString());
        facade.deploy(version, subdomain, imageName);
    }
}
