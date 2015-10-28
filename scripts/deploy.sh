#!/bin/sh

USER=$1
PUBLIC_IP=52.18.213.80

cd ..
mvn clean package
cp -v target/deployer.war scripts/deployment/
cd scripts/deployment/
: ${DEPLOYER_EMAIL_PASSWORD:?"Need to set DEPLOYER_EMAIL_PASSWORD as ENV var non-empty"}
envsubst '${DEPLOYER_EMAIL_PASSWORD}' < "./Dockerfile-tpl" > "./Dockerfile"
scp -r . ${USER}@${PUBLIC_IP}:/home/${USER}/deployer
ssh -t ${USER}@${PUBLIC_IP} "cd /home/${USER}/deployer/ && ./build.sh"
