package com.poolingpeople.deployer.dockerapi.boundary;

import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by hendrik on 09.12.15.
 */
@Named()
@SessionScoped
public class Test implements Serializable {
    String var = "Hallo";

    public String getVar() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.WARNING, "-Ich lese den String " + var+ " aus.");
        String copy = var + " copy";
        this.var = "Blub";
        return copy;
    }

    public void setVar(String var) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.WARNING, "-Ich setze den String " + var + " aus.");
        this.var = var;
    }

    public void something() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.WARNING, "-Something is called");
        var = "something";
    }

    public void pass(int value) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        logger.log(Level.WARNING, "-pass is called with " + value);
    }
}
