#!/bin/bash
sudo service docker stop
sudo docker -d -H="0.0.0.0:5555" -H unix:///var/run/docker.sock --api-enable-cors &