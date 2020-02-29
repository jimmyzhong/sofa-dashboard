#!/bin/bash
DIR=`dirname $0`
. $DIR/env.sh

if [ $# -lt 2 ] ; then
  echo "status.sh <jobId> <triggerId>"
  exit 1
fi
JOB_ID=$1
TRIGGER_ID=$2
echo "JOB_ID:$JOB_ID TRIGGER_ID:$TRIGGER_ID"

SERVER_NAME=sofa-jobs-agent-bootstrap

echo "command:ps -efc|grep java|grep isJobAgent|grep $USER|grep batch|grep SERVER_NAME=${SERVER_NAME}\\\\s|grep isJobAgent=true|grep jobId=${JOB_ID}|grep triggerId=${TRIGGER_ID}| wc -l"


count=`ps -efc|grep java|grep isJobAgent|grep $USER|grep batch|grep SERVER_NAME=${SERVER_NAME}\\\\s|grep isJobAgent=true|grep jobId=${JOB_ID}|grep triggerId=${TRIGGER_ID}| wc -l`
if [ $count -eq 0 ]; then
  echo "No running server found."
  exit 2
fi

if [ $count -gt 1 ]; then
  echo "${count} running servers found, please specify the exact server name."
  exit 1
fi

exit 0
