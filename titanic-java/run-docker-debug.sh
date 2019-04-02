#!/bin/sh
# first run docker login tdbanktest.azurecr.io with username tdbanktest

docker run  -e InputFile=/modeldata/train.arff -e OutputPath=/modeldata/ --entrypoint '/bin/sh' --mount type=bind,source=/modeldata,target=/modeldata -p 5005:5005 tdbanktest.azurecr.io/titanic-java -c 'exec /usr/bin/java -DInputFile=/modeldata/train.arff -DOutputPath=/modeldata/jtitanic -cp /modeldata/code/weka.jar:/modeldata/code/titanic.jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005  titanic.weka.Train'
