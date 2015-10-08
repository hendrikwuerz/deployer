package com.poolingpeople.deployer.scenario.boundary;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hendrik on 03.08.15.
 */
public class AWSInstances {



    /**
     * reloads and returns all available instances on aws
     * @return
     *      A list of all known instances
     */
    public static List<Instance> loadAvailableInstances(){
        AmazonEC2 ec2 = new AmazonEC2Client(new AWSCredentials());
        ec2.setRegion(Region.getRegion(Regions.EU_WEST_1));
        DescribeInstancesResult result = ec2.describeInstances();
        ArrayList<Instance> instances = new ArrayList<>();
        // get add reservations (if two instances are created at once only one reservation is set)
        result.getReservations().stream().forEach(res -> {
            List<Instance> resInst = res.getInstances();
            // Each reservation can have multiple instances
            resInst.stream().forEach(instances::add);
        });
        return instances;
    }

    /**
     * creates a list with all instances having a special key set as tag
     * @param key
     *          The key of the tag
     * @param instances
     *          A list with all instances. See loadAvailableInstances()
     * @return
     *          A sub-list with only instances having the wished tag
     */
    public static List<InstanceInfo> findInstance(String key, List<Instance> instances) {
        return findInstance(Arrays.asList(key), instances);
    }

    /**
     * creates a list with all instances having a special key set as tag
     * @param keys
     *          The returned instances must have a tag in this list. Only one matching tag is enough to be selected
     * @param instances
     *          A list with all instances. See loadAvailableInstances()
     * @return
     *          A sub-list with only instances having the wished tag
     */
    public static List<InstanceInfo> findInstance(List<String> keys, List<Instance> instances) {
        return instances.stream()
                .filter(instance -> instance.getTags().stream().filter(tag -> keys.contains(tag.getKey()) && tag.getValue().equals("true"))
                        .count() > 0) // check if there is a tag with key==true
                .map(InstanceInfo::new)
                .collect(Collectors.toList());
    }

    /**
     * checks if all instances with the passed IP are running.
     * checks for private and public IPs
     * @param ip
     *          The IP to be searched
     * @param instances
     *          The instances where search should be done
     * @return
     *          true if all instances (normally this should only be one ore none) with the passed IP are running
     *          false otherwise
     */
    public static boolean isIPRunning(String ip, List<Instance> instances) {
        long notRunningInstances = instances.stream()
                .map(InstanceInfo::new)
                .filter(instance -> {
                    String privateIP = instance.getPrivateIP();
                    String publicIP = instance.getPublicIP();
                    return (privateIP != null && privateIP.equals(ip)) ||
                            (publicIP != null && publicIP.equals(ip));
                }) // get instances where private or public IP is equals
                .filter(instance -> !instance.isStarted())
                .count();
        return notRunningInstances == 0;
    }

    /**
     * reloads available instances an searches for tags with the passed key
     * @param key
     *          The key of the tag
     * @return
     *          A sub-list with only instances having the wished tag
     */
    public static List<InstanceInfo> loadAvailableInstances(String key){
        return findInstance(key, loadAvailableInstances());
    }

    /**
     * reloads available instances an searches for tags with the passed key
     * @param keys
     *          All accepted keys
     * @return
     *          A sub-list with only instances having the wished tag
     */
    public static List<InstanceInfo> loadAvailableInstances(List<String> keys){
        return findInstance(keys, loadAvailableInstances());
    }

}
