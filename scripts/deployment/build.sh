#!/bin/sh
mkdir db
sudo docker build -t deployer .
sudo docker kill deployer
sudo docker rm deployer

sudo docker run -d --name deployer --restart=always -p 80:8080 -p 9990:9990 deployer
