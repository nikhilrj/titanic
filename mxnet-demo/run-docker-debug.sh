#!/bin/sh
# first run docker login tdbanktest.azurecr.io with username tdbanktest

docker run  -e DataPath=/modeldata/mxnet  --entrypoint '/bin/sh' \
                   --mount type=bind,source=/modeldata,target=/modeldata \
                   --mount type=bind,source=/data/home/bankadmin/projects/tdhackfest/mxnet-demo,target=/home/layer6/project \
                   -p 5005:5005 --rm tdbanktest.azurecr.io/mxnet-layer6:cuda92 \
                   -c 'cd project && ./startupDev.sh'
