# Connection Information Service

Simple webservice that dumps back request connection information.

# Basic Assumptions

1. Java Development Kit 8.0 ("JDK8") is installed and configured.
2. IntelliJ IDEA is installed and configured to use JDK8.
3. Developer has Kubernetes cluster access and can use the `kubectl` command.
4. The server is coded using `sparkjava` which is what the `assets-api` project used.
5. Project build and dependency resolution is handled by Gradle which is what the `assets-api` project used.
6. There is no "hot" reload functionality with `sparkjava` therefore to see code changes the below workflow is required:
    1. stop the old server.
    2. rebuild.
    3. start the new server.

# Quick Start: Greenfield Development

## 1. [Install Telepresence](https://www.telepresence.io/reference/install)

## 2. Open the service in IntelliJ IDEA

**NOTE:** These instructions were written on Linux. Key commands might be slightly different on macOS.

1. Start IntelliJ IDEA
2. `File > Open...` and navigate to and select the `connectioninfo` directory then click `OK`.
3. `Run > Edit Configurations` and open the `Run / Debug Configurations` window.
    1. Click the `+` symbol and select `Application`.
    2. Customize the below fields with the following information:
    
        ```text
        Name: run-server
        Main Class: connectioninfo.Server
        Use Classpath of Module: connectioninfo_main
        ```
4. Start the server: 
    - No Debugger: `Shift + F10`
    - Debugger: `Shift + F9`
    
Once the server is tarted you should see in the IDE log:

```text
2018-04-04 16:21:05.447 INFO [Thread-1] s.e.j.EmbeddedJettyServer - == Spark has ignited ...
2018-04-04 16:21:05.451 INFO [Thread-1] s.e.j.EmbeddedJettyServer - >> Listening on 0.0.0.0:4567
```

## 3. Start Telepresence

1. Start Telepresence with the following command:
    
   `telepresence --method vpn-tcp --new-deployment connectioninfo --expose 4567`

2. In IntelliJ start the server with `Shift + F10` and wait for the `Listening on 0.0.0.0:4567` message.

Once step 2 is complete your code is running locally and bridged to the Kubernetes cluster. You can talk to your service in three different ways:

1. From `localhost` to `localhost` which is not very useful: `curl localhost:4567/`
2. From `localhost` through Kubernetes using the automatically created Kubernetes service that maps to your code... `curl connectioninfo:4567/`
3. From Kubernetes back to your `localhost` where the code is running. For this we will use `cloudknife` which is a container with useful debugging tools pre-installed:

    ```bash
    kubectl apply -f https://raw.githubusercontent.com/datawire/cloudknife/master/cloudknife.yaml
    kubectl exec -it cloudknife -- /bin/sh
    
    curl connectioninfo:4567
    {
        "method" : "GET",
        "path" : "/",
        "queryParams" : { },
        "headers" : {
            "Accept" : "*/*",
            "User-Agent" : "curl/7.58.0",
            "Host" : "connectioninfo:4567"
        },
        "remoteAddress" : "127.0.0.1"
    }
    ```
## 4. Hack on a Change

Let's modify the `connectioninfo` server to add a new HTTP API endpoint. Copy paste the below code into the `main` method in `connectioninfo.Server`

```java
get("/hello/:name", (req, res) -> String.format("Hello, %s", req.params("name")));
```

Then reload the server in IntelliJ with `Shift + F5`. If you kept your `cloudknife` debug Kubernetes shell running then you should be able to do this:

```
curl connectioninfo:4567/hello/InvisionTeam

Hello, InvisionTeam
```

# Quick Start: Brownfield Development

In the previous scenario we went through the mechanics of using Telepresence to develop a new service using Telepresence, however, if you are working on an existing service the Telepresence command is slightly different.

This scenario closely tracks how we think Invision will most often use Telepresence when developing a webservice:

## 1. Setup

We want to mimic a world where a Kubernetes deployment and service already exists for the webservice we are about to modify so deploy to a cluster the initial version:

```bash
kubectl apply -f kubernetes/
```

We wait for the three `connectioninfo` pods to reach the `Ready` state.


```bash
kubectl get pods --selector=app=connectioninfo

NAME                              READY     STATUS    RESTARTS   AGE
connectioninfo-794f8477fb-2lqp5   1/1       Running   0          50m
connectioninfo-794f8477fb-pdkms   1/1       Running   0          50m
connectioninfo-794f8477fb-xj5zc   1/1       Running   0          50m
```

## 2. Start Telepresence

The command used previously was `--new-deployment` but this time instead we use `--swap-deployment` which causes the existing deployment to be swapped temporarily with a deployment that proxies to the developer's workstation:

    `telepresence --method vpn-tcp --swap-deployment connectioninfo --expose 4567`
    
Once this up and running you can, for example, start hacking on the `connectioninfo` Java server as done previously or even run a completely different server that listens on `:4567`.
    
## 3. Hack on a Change

Let's modify the `connectioninfo` server to add a new HTTP API endpoint. Copy paste the below code into the `main` method in `connectioninfo.Server`

```java
get("/walrus", (req, res) -> String.format("I am the walrus!") );
```

Then reload the server in IntelliJ with `Shift + F5`. If you kept your `cloudknife` debug Kubernetes shell running then you should be able to do this:

```
curl connectioninfo:4567/walrus

I am the walrus!
```
