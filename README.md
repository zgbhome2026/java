# Persistent Kubernetes shopping demo

This replaces the old static, browser-only shop with a Spring Boot application and a PostgreSQL database. It offers registration/login, three products, a per-user cart, checkout, and order history. Passwords are BCrypt hashed; product prices and orders are calculated by the server. **Checkout creates a demo order; there is no payment integration.** Do not enter card information.

This is a learning project. HTTP on a NodePort is not suitable for real credentials over an untrusted network. For a local lab, keep access on your trusted LAN, or use `kubectl port-forward` and HTTPS before exposing it more broadly. Sessions are stored in each app Pod and users log in again after an app restart; accounts, carts, and orders remain in PostgreSQL.

## Deploy to your cluster

The commands below run on a host with `kubectl`, `helm`, Docker for building the image, and SSH access to `worker03`. Copy this ZIP to the remote host where you run `kubectl` (for example, from your computer with `scp`), extract it there with `unzip shopping-project-persistent.zip`, and enter the `shopping-project` folder. If the build host and the Kubernetes worker are different machines, step 3 transfers the image to the worker. No live cluster is connected to this delivered ZIP; these commands perform the actual deployment on your cluster.

1. Check that `worker03` exists, `local-path-retain` exists, and port 30082 is unused:

   ```bash
   kubectl get nodes -o wide
   kubectl get storageclass local-path-retain
   kubectl get svc -A | grep 30082 || true
   ```

   The provisioner should already be working in your cluster. **Do not reapply** `manifests/local-path-provisioner.yaml` to a working installation. That example manifest is supplied for reference and its two previously broken ConfigMap keys have been corrected.

2. Build the image using the included, verified `src/app.jar`. This needs only the Java runtime base image on the remote host:

   ```bash
   docker build -f src/Dockerfile.prebuilt -t java-shopping-app:1.0.0 ./src
   ```

   To rebuild the JAR from source instead, use `docker build -t java-shopping-app:1.0.0 ./src`; that build downloads Maven dependencies and both base images.

3. If you built on `master01` and Kubernetes uses containerd on `worker03`, load the image into the Kubernetes image store of `worker03`:

   ```bash
   docker save java-shopping-app:1.0.0 | ssh worker03 'sudo ctr -n k8s.io images import -'
   ssh worker03 'sudo crictl images | grep java-shopping-app'
   ```

   The app is pinned to `worker03` in step 5. If Docker runs on `worker03`, run the build there and import the resulting image into containerd there. For a registry deployment, push the image and change `image.repository`, `image.pullPolicy`, and the node selector in chart values instead.

4. Create the namespace and a database password Secret. Use a new password; the command reads it without echoing it to the terminal. Keep it available for database recovery:

   ```bash
   kubectl apply -f manifests/namespace.yaml
   read -rsp 'New shopping database password: ' SHOP_DB_PASSWORD
   printf '\n'
   kubectl -n shopping-app create secret generic shopping-db-credentials \
     --from-literal=password="$SHOP_DB_PASSWORD" --dry-run=client -o yaml | kubectl apply -f -
   unset SHOP_DB_PASSWORD
   ```

5. Install the chart with a different NodePort from your current Tomcat (`30081`). The new application uses `30082`:

   ```bash
   helm upgrade --install shopping ./charts/shopping-chart \
     -n shopping-app \
     --set-json 'app.nodeSelector={"kubernetes.io/hostname":"worker03"}'
   kubectl -n shopping-app get pods,pvc,svc -o wide
   kubectl -n shopping-app rollout status deployment/shopping-postgres --timeout=180s
   kubectl -n shopping-app rollout status deployment/shopping-app --timeout=180s
   ```

   If `worker03` has a different Kubernetes node name, replace it in the command. If you pushed the image to a registry and all nodes can pull it, omit `--set-json` and set `image.repository` appropriately.

6. Visit `http://192.168.8.23:30082`. Click **Register**, choose a username/password, log in, add a product, check the cart, and save a demo order. **Orders** lists it. Your previous Tomcat page remains on `http://192.168.8.23:30081`.

## Verify PostgreSQL persistence

Create an order, then restart both Kubernetes Pods, without deleting the PVC:

```bash
kubectl -n shopping-app delete pod -l app.kubernetes.io/instance=shopping
kubectl -n shopping-app rollout status deployment/shopping-postgres --timeout=180s
kubectl -n shopping-app rollout status deployment/shopping-app --timeout=180s
kubectl -n shopping-app get pvc
```

Reload port 30082, log in again, and open **Orders**. Your order and account should remain. Note that `local-path-retain` keeps data on the selected worker's disk; a disk failure can still lose data. Back up PostgreSQL separately before storing anything important. Avoid `helm uninstall` or deleting the namespace during this test.

For troubleshooting:

```bash
kubectl -n shopping-app describe pod -l app.kubernetes.io/instance=shopping
kubectl -n shopping-app logs deployment/shopping-app --tail=100
kubectl -n shopping-app logs deployment/shopping-postgres --tail=100
kubectl -n shopping-app get pvc -o wide
```

If the app shows `ImagePullBackOff`, confirm the image is loaded in the containerd `k8s.io` namespace on the **same** worker as the app Pod. If PostgreSQL shows `Pending`, check the PVC, StorageClass, and provisioner logs. If port 30082 is in use, choose an unused NodePort in `charts/shopping-chart/values.yaml` and rerun `helm upgrade --install`.

The old `helm-repo/` archive was removed because it packaged the original empty application and did not reflect this code. The chart source in `charts/shopping-chart/` is authoritative.
