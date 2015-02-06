package com.poolingpeople.deployer.boundary;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by alacambra on 2/6/15.
 */
@Named
public class DeployerUiController {

    @Inject
    DeployerFacade facade;

    private String subdomain;
    private String version;

    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void deploy(){

    }
}
