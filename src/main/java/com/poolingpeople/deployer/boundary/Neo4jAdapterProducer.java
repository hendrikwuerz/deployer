package com.poolingpeople.deployer.boundary;

import com.poolingpeople.utils.neo4jApi.Endpoint;
import com.poolingpeople.utils.neo4jApi.Neo4jRestApiAdapter;
import com.poolingpeople.utils.neo4jApi.Neo4jRestApiAdapterImpl;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

/**
 * Created by alacambra on 17.02.15.
 */
@Alternative
@RequestScoped
public class Neo4jAdapterProducer {

    String host = "localhost";
    Integer port = 7474;

    @Produces
    public Neo4jRestApiAdapter get(){
        return new Neo4jRestApiAdapterImpl(new Endpoint(host, port));
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
