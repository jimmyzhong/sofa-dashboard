#/bin/sh

if [ $# -ne 1 ] ; then
  echo "stop <Server>"
  exit 1
fi

SERVER_NAME=$1

DIR=`dirname $0`
. $DIR/env.sh

cd $BIN_DIR

count=`ps -efc|grep java|grep -v batch|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|wc -l`
if [ $count -eq 0 ]; then
  echo "No running server found."
  exit 0
fi

if [ $count -gt 1 ]; then
  echo "${count} running servers found, please specify the exact server name."
  exit 1
fi


#先正常停止服务
PIDS=`ps -efc|grep java|grep -v batch|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|awk -F' ' '{print $2}'`
for PID in $PIDS
do
  echo "Print current stack information for PID ${PID} to stack.txt"
  jstack ${PID} > stack.txt
  echo "Kill pid ${PID}."
  kill $PID
done

#检查服务是否停止，最多90秒
for((i=1;i<=90;i++))
do 
  count=`ps -efc|grep java|grep -v batch|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|wc -l`
  if [ $count -eq 0 ]; then
    echo "Server has been stopped."
    exit 0
  fi
  echo "Waiting for ${count} server(s) stopped: ${i}"
  sleep 1
done

#强制停止服务
PIDS=`ps -efc|grep java|grep -v batch|grep $USER|grep SERVER_NAME=${SERVER_NAME}\\\\s|awk -F' ' '{print $2}'`
for PID in $PIDS
do
  echo "Kill pid $PID forcely."
  kill -9 $PID
done

exit 0

