echo "setting up databricks cli"
python -m pip install --upgrade pip setuptools wheel
pip install --upgrade pip
pip install databricks-cli

echo "configuring databricks cli"
conf=`cat << EOM
$DATABRICKS_HOST
$DATABRICKS_TOKEN
EOM`
 
echo "$conf" | databricks configure --token
 
echo "copying new files to databricks cluster"
databricks workspace import_dir notebooks/Shared/ /Shared/ -o
 
echo "turning on cluster"
 
run the job
databricks jobs run-now --job-id 2