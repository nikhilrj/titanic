lsb_release -a
wget https://packages.microsoft.com/config/ubuntu/16.04/packages-microsoft-prod.deb
sudo dpkg -i packages-microsoft-prod.deb
sudo apt-get update
sudo apt-get install blobfuse

sudo mkdir /mnt/blobfusetmp
sudo chown <youruser> /mnt/blobfusetmp

echo accountName myaccount >> blobfuse_connection.cfg
echo accountKey storageaccesskey >> blobfuse_connection.cfg
echo containerName mycontainer >> blobfuse_connection.cfg

sudo mkdir /modeldata

sudo blobfuse /modeldata --tmp-path=/mnt/blobfusetmp  --config-file=/path/to/fuse_connection.cfg -o attr_timeout=240 -o entry_timeout=240 -o negative_timeout=120 

#Allow other users to access the blob
# sudo blobfuse /modeldata --tmp-path=/mnt/blobfusetmp  --config-file=/path/to/fuse_connection.cfg -o attr_timeout=240 -o entry_timeout=240 -o negative_timeout=120 -o allow_other