package com.poolingpeople.deployer.docker.boundary;

/**
 * Created by alacambra on 09.02.15.
 * "HostConfig": {
 "Binds": ["/tmp:/tmp"],
 "Links": ["redis3:redis"],
 "LxcConf": {"lxc.utsname":"docker"},
 "PortBindings": { "22/tcp": [{ "HostPort": "11022" }] },
 "PublishAllPorts": false,
 "Privileged": false,
 "Dns": ["8.8.8.8"],
 "DnsSearch": [""],
 "ExtraHosts": null,
 "VolumesFrom": ["parent", "other:ro"],
 "CapAdd": ["NET_ADMIN"],
 "CapDrop": ["MKNOD"],
 "RestartPolicy": { "Name": "", "MaximumRetryCount": 0 },
 "NetworkMode": "bridge",
 "Devices": []
 }
 */
public class HostConfigBodyBuilder {



}
