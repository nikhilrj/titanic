# titanic #

Example code for solving the Titanic prediction problem (https://www.kaggle.com/c/titanic-gettingStarted) found on Kaggle.  This example uses the Weka Data Mining Libraries to perform our classifications and predictions. Note, we are using Weka version 3.6.9.

## Data Cleanup/Initialization ##

Before we begin, we have to clean up the data files provided by Kaggle (these cleanup steps have already been performed on the committed files).  The first step is to remove the nested '""' (quotation marks) from the files.  This was simply a straight search and replace operation in my editor.

The next step is to convert the CSV formatted files into the ARFF format.  The ARFF format provides more detailed information about the type of data in the CSV files.  To perform this conversion, you can use the CSVLoader from the Weka libraries.

```
java -cp lib/weka.jar weka.core.converters.CSVLoader test.csv > test.arff
java -cp lib/weka.jar weka.core.converters.CSVLoader train.csv > train.arff
```

Once we have created the ARFF files, we need to clean them up a little bit.  First, we identify any 'string' column to be of type string, and not nominal.  Then we ensure that nominal values are in the same order for both files (VERY IMPORTANT!).  Here is what the header section of the ARFF file should look like:

```
@attribute survived {0,1}
@attribute pclass numeric
@attribute name string
@attribute sex {male,female}
@attribute age numeric
@attribute sibsp numeric
@attribute parch numeric
@attribute ticket string
@attribute fare numeric
@attribute cabin string
@attribute embarked {Q,S,C}
```

## Training, Predicting, and Verifying the data ##

Now that we have cleaned up our data, we are ready to run the code.  I have included the Eclipse project files to make it easy for anyone to import this project into Eclipse and go.  I have also included an Ant build file to compile and run everything as well.  If you don't have either of those options, you are on your own.

### Training ###

To train the classifier, execute the 'titanic.weka.Train' class or run 'ant train' in a terminal.  This will load the training data, create and train a Classifier, and write the Classifier to disk.

### Predicting ###

To create a prediction, execute the 'titanic.weka.Predict' class or run 'ant predict'.  This will load the test data, read the trained Classifier from disk, and produce a 'predict.csv'.  This CSV file is in a suitable format to submit to Kaggle.

### Verifying ###

To verify our predictions, execute the 'titanic.weka.Verify' class or run 'ant verify'.  This will load our prediction results, read the trained Classifier from disk, then evaluate the classification performance.  You will see output similar to this:

```
Correctly Classified Instances         418              100      %
Incorrectly Classified Instances         0                0      %
Kappa statistic                          1     
Mean absolute error                      0.1409
Root mean squared error                  0.1986
Relative absolute error                 30.3515 %
Root relative squared error             41.2246 %
Total Number of Instances              418     
```

# Mount blobfuse

Configure Microsoft repository
```
wget https://packages.microsoft.com/config/ubuntu/16.04/packages-microsoft-prod.deb
sudo dpkg -i packages-microsoft-prod.deb
sudo apt-get update
```
Install blobfuse
```
sudo apt-get install blobfuse fuse
```

Create directory in ephemeral disk or RAM disk
```
mkdir -p /mnt/blobfusetmp
```

Create fuse_connection.cfg
```
accountName tdhackfestblob
accountKey r7wL0PGKnFTNkA9oDNRjkSAAJEvPIQaaZCjAR19cfPciZLyMjPsYJj6UO+dxoDCIMG/HABZZtBArbtJ15/C52g==
containerName tdhackfestcontainer
```

Mount blobfuse
```
sudo blobfuse /modeldata --tmp-path=/mnt/blobfusetmp --config-file=/config/fuse_connection.cfg -o attr_timeout=240 -o entry_timeout=240 -o negative_timeout=120
```

# Run Model in docker

Build container, login to docker registry  and run `build-docker.sh` 


```
docker run  -e InputFile=/modeldata/train.arff -e OutputPath=/modeldata/ --mount type=bind,source=/modeldata,target=/modeldata tdbanktest.azurecr.io/titanic-java
```

override to load java code from blob
```
docker run  -e InputFile=/modeldata/train.arff -e OutputPath=/modeldata/ --mount type=bind,source=/modeldata,target=/modeldata tdbanktest.azurecr.io/titanic-java --entrypoint '/bin/sh' -c '/usr/bin/java -DInputFile=/modeldata/train.arff -DOutputPath=/modeldata/jtitanic -cp /modeldata/code/weka.jar:/modeldata/code/titanic.jar  -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 titanic.weka.Train'
```

# Java Remote debug

Open Port on VM
```
az vm open-port --resource-group TD-Bank-ML-Test --name MLworkstation --port 5005 --priority 1001
```

Run docker with port exposed and JDWP enabled for all connections
```
docker run  -e InputFile=/modeldata/train.arff -e OutputPath=/modeldata/ --entrypoint '/bin/sh' --mount type=bind,source=/modeldata,target=/modeldata -p 5005:5005 tdbanktest.azurecr.io/titanic-java -c 'exec /usr/bin/java -DInputFile=/modeldata/train.arff -DOutputPath=/modeldata/jtitanic -cp /modeldata/code/weka.jar:/modeldata/code/titanic.jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005  titanic.weka.Train'
```

or just run `run-docker-debug.sh`

IntelliJ Config for remote debug

```
   <configuration name="docker-remote" type="Remote" factoryName="Remote">
      <module name="titanic" />
      <option name="USE_SOCKET_TRANSPORT" value="true" />
      <option name="SERVER_MODE" value="false" />
      <option name="SHMEM_ADDRESS" value="javadebug" />
      <option name="HOST" value="52.183.124.53" />
      <option name="PORT" value="5005" />
      <RunnerSettings RunnerId="Debug">
        <option name="DEBUG_PORT" value="5005" />
        <option name="LOCAL" value="false" />
      </RunnerSettings>
    </configuration>
```