#!/bin/sh

USER=$1
PUBLIC_IP=52.17.63.225

cd ..
mvn clean package
cp target/deployer.war scripts/deployment/
cd scripts/deployment/
scp -r . ${USER}@${PUBLIC_IP}:/home/${USER}/deployer
ssh -t ${USER}@${PUBLIC_IP} "cd /home/${USER}/deployer/ && ./build.sh"
