package com.poolingpeople.deployer.dockerapi.boundary;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by hendrik on 20.03.15.
 */
public class CacheInfo {

    File cachedFile;

    public CacheInfo(File cachedFile) {
        this.cachedFile = cachedFile;
    }

    public void setCachedFile(File cachedFile) {
        this.cachedFile = cachedFile;
    }

    public File getCachedFile() {
        return cachedFile;
    }

    public String getName() {
        return cachedFile.getName();
    }

    public long getLength() {
        return cachedFile.length();
    }

    public String getLastModified() {
        long diff = System.currentTimeMillis() - cachedFile.lastModified();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff - TimeUnit.DAYS.toMillis(days));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes));

        return "Modified " + days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds ago";
    }

    public boolean delete() {
        return cachedFile.delete();
    }
}
