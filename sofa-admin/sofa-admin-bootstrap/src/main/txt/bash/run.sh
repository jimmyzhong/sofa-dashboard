#/bin/bash

DIR=`dirname $0`
echo "DIR:$DIR"

. $DIR/env.sh
echo "RUN_ENV:$RUN_ENV"
echo "APP_DIR:$APP_DIR"

SERVER_NAME=sofa-jobs-agent-bootstrap

PARAMS=$*
echo "PARAMS:$PARAMS"

LAUNCHER_CLASS=org.springframework.boot.loader.JarLauncher
export USER_MEM_ARGS="-XX:MetaspaceSize=128M  -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC "

export JAVA_OPTIONS="${USER_MEM_ARGS} $PARAMS -Djava.awt.headless=true -DSERVER_NAME=$SERVER_NAME -Drun_env=${RUN_ENV} -Dproduct_mode=${PRODUCT_MODE} \
-Dspring.profiles.active=batch,${RUN_ENV} -Djobs.log.dir=${LOG_DIR}/joblogs -Djobs.script.dir=${BIN_DIR} -Djava.io.tmpdir=${TMP_DIR} \
-Dcom.alipay.sofa.rpc.virtual-host=$LOCAL_IP -Dcom.alipay.sofa.rpc.registry.address=${ZOOKEEPER_IP} \
-Dspring.main.web-application-type=none -Djava.library.path=${BASE_DIR}/lib -Dlogging.config=classpath:logback_batch.xml "

JAR_FILES=`find "$LIB_DIR" -name "*.jar" | paste -d: -s`
echo CLASSPATH JARS:$JAR_FILES

echo ">> run #: $JAVA_HOME/bin/java $USER_MEM_ARGS $JAVA_OPTIONS -cp "$JAR_FILES:$APP_DIR/$SERVER_NAME.jar" $LAUNCHER_CLASS"
$JAVA_HOME/bin/java $USER_MEM_ARGS $JAVA_OPTIONS -cp "$JAR_FILES:$APP_DIR/$SERVER_NAME.jar" $LAUNCHER_CLASS
