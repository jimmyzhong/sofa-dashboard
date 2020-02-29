#!/bin/bash
DIR=`dirname $0`
. $DIR/env.sh


if [ $# -ne 1 ] ; then
  echo "sm <Server>"
  exit 1
fi

function in_array()
{
    local zs_array=${SUPPORT_SERVERS[@]};	
	for s_name in $zs_array;do
	  #echo ".$s_name.test"
	  if test $s_name = $1 ; then
		return 1;
	  fi
	done
	return 0
}

SUPPORT_SERVERS=('sofa-admin-bootstrap' 'sofa-jobs-bootstrap' 'sofa-jobs-agent-bootstrap');

SERVER_NAME=$1
SERVER_SHORT_NAME=`echo $SERVER_NAME | sed 's/[0-9]*$//'`


count=`ps -efc|grep java|grep -v batch|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|wc -l`
if [ $count -ge 1 ]; then
  echo "${SERVER_NAME} already running"
  exit 0
fi

in_array $SERVER_NAME
ret=$?
if [ $ret -eq 0 ]; then
	echo "$SERVER_NAME not support"
	exit 0
fi

echo "SERVER_NAME:$SERVER_NAME"

#export USER_MEM_ARGS="-Xms256m -Xmx768m -XX:PermSize=128M -XX:MaxPermSize=420M"
export USER_MEM_ARGS="-XX:MetaspaceSize=128M  -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC "


#debug


JAVA_OPTIONS="-Djava.awt.headless=true -Drun_env=${RUN_ENV} -Dproduct_mode=${PRODUCT_MODE} \
	-Dspring.profiles.active=${RUN_ENV} -Dtrace.prefix=${TRACE_PREFIX} \
	-DSERVER_NAME=${SERVER_NAME} -Ddubbo.protocol.host=$LOCAL_IP \
	-Dcom.alipay.sofa.rpc.virtual-host=$LOCAL_IP -Dcom.alipay.sofa.rpc.registry.address=${ZOOKEEPER_IP} \
	-Dspring.data.mongodb.uri=${MONGO_URI} \
	-Djobs.log.dir=${LOG_DIR}/joblogs -Djobs.script.dir=${BIN_DIR} \
	-Djava.io.tmpdir=${TMP_DIR} -Dlogging.config=${ETC_DIR}/logback.xml -Drocketmq.client.logRoot=${LOG_DIR}/mqlog \
	-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Dfile.encoding=${FILE_ENCODING} -Djava.library.path=${BASE_DIR}/lib"

export JAVA_OPTIONS
	
echo "JAVA_OPTIONS:${JAVA_OPTIONS}"

rm -rf $DOMAIN_DIR/servers/$SERVER_NAME/tmp

cd $BIN_DIR
echo "USER_MEM_ARGS:$USER_MEM_ARGS"
#echo "JAVA_OPTIONS:$JAVA_OPTIONS"
echo LOG_DIR: $LOG_DIR/$SERVER_NAME.log

if [ -e "$APP_DIR/$SERVER_NAME.jar" ];then
  LAUNCHER_CLASS=org.springframework.boot.loader.JarLauncher
  echo "LAUNCHER_CLASS:$LAUNCHER_CLASS"
  nohup $JAVA_HOME/bin/java $USER_MEM_ARGS $JAVA_OPTIONS -cp "$APP_DIR:$APP_DIR/$SERVER_NAME.jar" $LAUNCHER_CLASS > $LOG_DIR/$SERVER_NAME.log 2>&1 &
elif [ -e "$APP_DIR/$SERVER_NAME.war" ];then
  LAUNCHER_CLASS=org.springframework.boot.loader.WarLauncher
  echo "LAUNCHER_CLASS:$LAUNCHER_CLASS"
  nohup $JAVA_HOME/bin/java $USER_MEM_ARGS $JAVA_OPTIONS -cp "$APP_DIR:$APP_DIR/$SERVER_NAME.war" $LAUNCHER_CLASS > $LOG_DIR/$SERVER_NAME.log 2>&1 &
else
  echo "$SERVER_NAME.jar or $SERVER_NAME.war not exist"
  exit 0
fi
echo Server $SERVER_NAME started.

