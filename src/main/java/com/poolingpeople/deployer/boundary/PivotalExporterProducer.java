package com.poolingpeople.deployer.boundary;

import com.poolingpeople.search.solr.PivotalExporter;

import javax.inject.Inject;

/**
 * Created by alacambra on 17.02.15.
 */
public class PivotalExporterProducer {

    @Inject
    PivotalExporterProducer producer;

    @Inject
    PivotalExporter exporter;

    public PivotalExporter get(){
        producer.get();
        return exporter;
    }
}
