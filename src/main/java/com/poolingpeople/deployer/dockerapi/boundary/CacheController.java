package com.poolingpeople.deployer.dockerapi.boundary;

import com.poolingpeople.deployer.application.boundary.VersionsApi;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.CollectionDataModel;
import javax.faces.model.DataModel;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by hendrik on 17.03.15.
 */

@Named
@RequestScoped
public class CacheController {

    @Inject
    VersionsApi versionsApi;

    DataModel<CacheInfo> cachedFiles;

    public DataModel<CacheInfo> getCachedFiles() {
        Collection<CacheInfo> cacheInfos = new ArrayList<>();
        Collection<File> files = versionsApi.getCachedFiles(); // all cached files
        files.forEach( file -> cacheInfos.add(new CacheInfo(file)));
        cachedFiles = new CollectionDataModel<>(cacheInfos);
        return cachedFiles;
    }

    public String destroy() {
        CacheInfo current = cachedFiles.getRowData();
        boolean deleted = current.getCachedFile().delete();
        System.out.println("File deleted: " + deleted);
        return "cache-list";
    }
}
