#!/bin/sh
mkdir db
sudo docker build --tag=deployer /home/ubuntu/deployer/
sudo docker kill deployer
sudo docker rm deployer

sudo docker run -d -p 8080:8080 deployer
