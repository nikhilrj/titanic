mvn package exec:exec -DskipTests -Dexec.executable="java" -Dexec.args="-classpath %classpath -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 mxnet.App"
