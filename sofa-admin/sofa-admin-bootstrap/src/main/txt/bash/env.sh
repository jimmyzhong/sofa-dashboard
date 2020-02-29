#!/bin/sh

export BASE_DIR=/home/server/jobs
export APP_DIR=$BASE_DIR/application
export BIN_DIR=$BASE_DIR/bin
export LOG_DIR=$BASE_DIR/logs
export ETC_DIR=$BASE_DIR/etc
export LIB_DIR=$BASE_DIR/lib
export TMP_DIR=$BASE_DIR/tmp

export FILE_ENCODING=utf8
export JAVA_VENDOR=Sun
export PRODUCT_MODE=t
export RUN_ENV=agent,dev
export TRACE_PREFIX=X0
export ZOOKEEPER_IP=zookeeper://10.10.51.212:2181
export MONGO_URI=mongodb://10.10.51.212:27017/jobs

export JAVA_HOME=/usr/local/jdk1.8.0_121
export PATH=$JAVA_HOME/bin:$PATH

JAR_FILES=`find $LIB_DIR -name "*.jar" | paste -d: -s`
export MY_CLASSPATH=$ETC_DIR:$JAR_FILES

export LOCAL_IP=`LC_ALL=C /sbin/ifconfig eth0|grep "inet addr:"|cut -d: -f2|awk '{print $1}'`
