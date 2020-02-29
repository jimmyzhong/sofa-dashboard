#/bin/sh

DIR=`dirname $0`
. $DIR/env.sh

if [ $# -ne 1 ] ; then
  echo "restart.sh <Server>"
  exit 1
fi


SERVER_NAME=$1

$DIR/term.sh $SERVER_NAME
$DIR/boot.sh $SERVER_NAME

