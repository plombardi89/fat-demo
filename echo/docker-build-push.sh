#!/usr/bin/env bash
set -o nounset
set -o errexit

docker build -t plombardi89/echo:latest .
docker push plombardi89/echo:latest
