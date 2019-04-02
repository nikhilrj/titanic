#!/bin/sh

# compile debug
echo " ========= Compiling =========="
javac -cp lib/weka.jar:. -d bin/ -g src/titanic/weka/*.java

cd bin && jar -cvf ../lib/titanic.jar .
cd ..

## First run docker login tdbanktest.azurecr.io with username tdbanktest
echo " ========= Building docker =========="
docker build -t tdbanktest.azurecr.io/titanic-java .
docker push tdbanktest.azurecr.io/titanic-java