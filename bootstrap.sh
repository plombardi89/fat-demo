#!/usr/bin/env bash

export KUBECONFIG=~/.kube/kubernaut
kubectl apply -f foundation.yaml
