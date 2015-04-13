package com.poolingpeople.deployer.boundary;

import com.poolingpeople.deployer.dockerapi.boundary.DockerApi;
import com.poolingpeople.deployer.dockerapi.boundary.DockerEndPoint;
import org.junit.Before;
import org.junit.Test;

public class DbSnapshotMakerManagerTestIT {

    DbSnapshotManagerFacade cut;

    @Before
    public void init(){
        cut = new DbSnapshotManagerFacade(new DockerApi(new DockerEndPoint("prod.poolingpeople.com", 5555, "http")));
    }

    @Test
    public void testMakeSnapshot() throws Exception {
//        cut.makeSnapshot("3d96af9b48", "/var/lib/neo4j/data/graph.db/", "testDb");
    }

    @Test
    public void testUploadSnapshot() throws Exception {

    }

//    @Test
//    public void testFetchSnapshot(){
//        cut.fetchSnapshot("testDb");
//    }
}