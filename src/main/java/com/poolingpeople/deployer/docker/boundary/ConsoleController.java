package com.poolingpeople.deployer.docker.boundary;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by alacambra on 09.02.15.
 */

@Named
public class ConsoleController {

    @Inject
    DockerApi api;

    private String selectedContainerId = "";
    private String logs;

    public void showLogs(){
        logs = api.getContainersLogs(selectedContainerId, 50);
    }

    public void setSelectedContainerId(String selectedContainerId) {
        this.selectedContainerId = selectedContainerId;
    }

    public String getSelectedContainerId() {
        return selectedContainerId;
    }

    public String getLogs() {
        return logs;
    }
}
