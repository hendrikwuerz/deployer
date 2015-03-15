#!/bin/sh
cd ..
mvn clean package
cp target/deployer.war scripts/deployment/
cd scripts/deployment/
scp -r . ubuntu@deployer.poolingpeople.com:/home/ubuntu/deployer
