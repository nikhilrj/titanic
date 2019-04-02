kubectl apply -f https://raw.githubusercontent.com/Azure/kubernetes-volume-drivers/master/flexvolume/blobfuse/deployment/blobfuse-flexvol-installer-1.9.yaml
kubectl create secret generic blobfusecreds --from-literal accountname=ACCOUNT-NAME --from-literal accountkey="ACCOUNT-KEY" --type="azure/blobfuse"
kubectl create -f pv-blobfuse-flexvol.yaml
kubectl create -f pvc-blobfuse-flexvol.yaml
kubectl create -f (pod or deployment)
