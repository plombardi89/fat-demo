#!/usr/bin/env bash
set -o nounset
set -o errexit

docker build -t plombardi89/connectioninfo:latest .
docker push plombardi89/connectioninfo:latest
