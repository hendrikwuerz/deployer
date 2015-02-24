package com.poolingpeople.deployer.application.boundary;

import java.io.InputStream;

/**
 * Created by alacambra on 24.02.15.
 */
public class LocalStorage {

    private String localPath;

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void persist(InputStream file){

    }

    public InputStream getFile(){
        return null;
    }

}
