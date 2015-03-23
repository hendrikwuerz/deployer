#!/bin/sh
cd ..
mvn clean package
cp target/deployer.war scripts/deployment/
cd scripts/deployment/
scp -r . ubuntu@deployer.poolingpeople.com:/home/ubuntu/deployer
ssh ubuntu@deployer.poolingpeople.com "cd /home/ubuntu/deployer/ && ./build.sh"
