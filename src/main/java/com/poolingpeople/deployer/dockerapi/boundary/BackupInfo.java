package com.poolingpeople.deployer.dockerapi.boundary;

import java.io.Serializable;

/**
 * Created by hendrik on 20.04.15.
 */
public class BackupInfo {

    ContainerInfo container;
    String host;
    boolean backup;

    public BackupInfo(ContainerInfo container, String host) {
        this(container, host, false);
    }

    public BackupInfo(ContainerInfo container, String host, boolean backup) {
        this.container = container;
        this.host = host;
        this.backup = backup;
    }

    public ContainerInfo getContainer() {
        return container;
    }

    public void setContainer(ContainerInfo container) {
        this.container = container;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }
}
