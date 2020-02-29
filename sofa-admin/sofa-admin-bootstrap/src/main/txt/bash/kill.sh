#!/bin/bash
DIR=`dirname $0`
. $DIR/env.sh

if [ $# -lt 2 ] ; then
  echo "kill.sh <jobId> <triggerId>"
  exit 1
fi
JOB_ID=$1
TRIGGER_ID=$2
echo "kill.sh JOB_ID:$JOB_ID TRIGGER_ID:$TRIGGER_ID"

SERVER_NAME=sofa-jobs-agent-bootstrap

echo "command:ps -efc|grep java|grep isJobAgent|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|grep isJobAgent=true|grep -DjobId=${JOB_ID}|grep -DtriggerId=${TRIGGER_ID} | wc -l"


count=`ps -efc|grep java|grep isJobAgent|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|grep isJobAgent=true|grep jobId=${JOB_ID}|grep triggerId=${TRIGGER_ID}| wc -l`
if [ $count -eq 0 ]; then
  echo "No running server found."
  exit 0
fi

if [ $count -gt 1 ]; then
  echo "${count} running servers found, please specify the exact server name."
  exit 1
fi


#先正常停止服务
PIDS=`ps -efc|grep java|grep isJobAgent|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|grep isJobAgent=true|grep jobId=${JOB_ID}|grep triggerId=${TRIGGER_ID}|awk -F' ' '{print $2}'`
for PID in $PIDS
do
  echo "normal kill PID ${PID}"
  echo "Kill pid ${PID}."
  kill $PID
done

#检查服务是否停止，最多10秒
for((i=1;i<=10;i++))
do
  count=`ps -efc|grep java|grep isJobAgent|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|grep isJobAgent=true|grep jobId=${JOB_ID}|grep triggerId=${TRIGGER_ID}|wc -l`
  if [ $count -eq 0 ]; then
    echo "Server has been stopped."
    exit 0
  fi
  echo "Waiting for ${count} server(s) stopped: ${i}"
  sleep 1
done

#强制停止服务
PIDS=`ps -efc|grep java|grep isJobAgent|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|grep isJobAgent=true|grep jobId=${JOB_ID}|grep triggerId=${TRIGGER_ID}|awk -F' ' '{print $2}'`
for PID in $PIDS
do
  echo "Kill pid $PID forcely."
  kill -9 $PID
done

exit 0
