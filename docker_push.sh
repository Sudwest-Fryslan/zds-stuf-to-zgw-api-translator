#!/bin/sh

# error on unset variables, exit on error
set -eu
set +x

rm -f ./src/main/resources/application.properties
rm -f ./src/main/resources/config.json

mkdir ./data/tests
cp -R ./src/test/resources/ladybug/* /data/tests

mvn install -Dmaven.javadoc.skip=true -B -V -DskipTests

# Echo script commands
set -x

git_tag=${tag_name}

REPO=openzaakbrug/openzaakbrug
TAG=${git_tag:-latest}

# Build the image
docker build -t $REPO:$TAG .

# Push the image
docker push $REPO:$TAG
