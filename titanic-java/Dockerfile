# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine

ENV InputFile /usr/share/train.arff
ENV OutputPath /usr/share/

# COPY
COPY train.arff /usr/share/train.arff

# copy WAR into image
COPY lib/weka.jar /weka.jar
COPY lib/titanic.jar  /titanic.jar

EXPOSE 5005

# run application with this command line
CMD ["/usr/bin/java", "-DInputFile=$InputFile", "-DOutputPath=$OutputPath", "-cp", "weka.jar:titanic.jar", "titanic.weka.Train"]