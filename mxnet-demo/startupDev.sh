mvn package exec:exec -P linux-x86_64 -Dexec.executable="java" -Dexec.args="-classpath %classpath -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 mxnet.ObjectDetection"
