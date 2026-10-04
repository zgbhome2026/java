# ImagePullBackOff checks

See `README.md` for the complete deploy steps.

The new app image is `java-shopping-app:1.0.0`, deployed to `shopping-app` namespace and pinned to `worker03` when you use the documented Helm command. Build it on your remote build host and import the saved image into containerd's `k8s.io` namespace on `worker03`:

```bash
docker build -f src/Dockerfile.prebuilt -t java-shopping-app:1.0.0 ./src
docker save java-shopping-app:1.0.0 | ssh worker03 'sudo ctr -n k8s.io images import -'
ssh worker03 'sudo crictl images | grep java-shopping-app'
kubectl -n shopping-app get pods -o wide
kubectl -n shopping-app describe pod -l app.kubernetes.io/component=app
```

If the node is not `worker03`, update the Helm node selector and import the image on the actual node. For a registry, set the chart image repository to the full pushed image name and use the appropriate pull secret if private.
