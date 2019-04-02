RESOURCE_GROUP="TD-Bank-ML-Test"
VNET="databricks-vnet"
az group create -n "$RESOURCE_GROUP" --location westus2
az group deployment create --resource-group "$RESOURCE_GROUP" --name data-bricks-deployment --template-uri https://raw.githubusercontent.com/Azure/azure-quickstart-templates/master/101-databricks-all-in-one-template-for-vnet-injection/azuredeploy.json --parameters workspaceName="tdworkspace"
az network vnet subnet create -n k8s -g "$RESOURCE_GROUP" --vnet-name "$VNET" --address-prefix 10.179.128.0/24
az network vnet subnet create -n admin -g "$RESOURCE_GROUP" --vnet-name "$VNET" --address-prefix 10.179.129.0/24

K8S_SUBNET_ID=$(az network vnet subnet show -n k8s -g "$RESOURCE_GROUP" --vnet-name "$VNET" --query id -o tsv 2>&1)

SERVICE_PRINCIPAL=$(az ad sp create-for-rbac --skip-assignment -n "aks-td-sp-delete" --years 3 --query '[appId, password]' -o tsv)

SP=$(echo $SERVICE_PRINCIPAL | tr -d '\n')

read APPID CLIENT_SECRET <<< "$SP"

echo $APPID

echo $CLIENT_SECRET

az aks create -n "l6-aks-cluster" -g "$RESOURCE_GROUP" -u bankadmin -a monitoring  --service-principal $APPID --client-secret $CLIENT_SECRET \
--enable-cluster-autoscaler --enable-vmss --kubernetes-version 1.12.6 -l westus2 --max-count 5 -m 40 --min-count 2 --network-plugin azure \
-c 3 --vnet-subnet-id $K8S_SUBNET_ID -s Standard_D4s_v3   --dns-service-ip 10.179.130.10 --service-cidr 10.179.130.0/24

az vm create -n "data-science-workstation" -g "$RESOURCE_GROUP" --admin-username datascientist --admin-password tdD@TaSc13nC3 --image "microsoft-ads:linux-data-science-vm-ubuntu:linuxdsvmubuntu:18.08.00" --vnet-name "$VNET" --subnet admin --size Standard_NC12