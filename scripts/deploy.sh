#!/bin/sh
cd ..
mvn clean package
cp target/deployer.war scripts/deployment/
cd scripts/deployment/
scp -r . ubuntu@54.154.110.209:/home/ubuntu/deployer
