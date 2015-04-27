package com.poolingpeople.deployer.dockerapi.boundary;

import java.io.Serializable;

/**
 * Created by hendrik on 20.04.15.
 */
public class BackupInfo {

    ContainerInfo container;
    boolean backup;

    public BackupInfo(ContainerInfo container) {
        this(container, false);
    }

    public BackupInfo(ContainerInfo container, boolean backup) {
        this.container = container;
        this.backup = backup;
    }

    public ContainerInfo getContainer() {
        return container;
    }

    public void setContainer(ContainerInfo container) {
        this.container = container;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }
}
