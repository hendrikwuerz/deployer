package com.poolingpeople.deployer.control;

import com.poolingpeople.deployer.entity.ClusterConfig;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;

/**
 * Created by alacambra on 2/6/15.
 */
public class ClusterConfigProvider {

//    @PersistenceContext
    EntityManager em;

    public Collection<ClusterConfig> getCurrentClusters(String domain){

        List<ClusterConfig> clusterConfigs =
                em.createNamedQuery(ClusterConfig.getAllClusters)
                        .setParameter("serverDomain", domain)
                        .getResultList();

        return clusterConfigs;
    }

}
