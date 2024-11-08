#!/bin/bash
#
#
PROFILE=ics
THIS_SCRIPT_FILE_NAME=install-ics-$PROFILE.sh
DEPLOY_SCRIPT_FILE_NAME=deploy.sh
HOME_DIR=/home/andold
SOURCE_DIR=$HOME_DIR/src/github/ics
DEPLOY_DIR=$HOME_DIR/deploy/ics
TOMCAT_BIN_DIR=$HOME_DIR/apps/tomcat/bin
#
# source download
#
git config --global core.quotepath false
#
#
cd	$SOURCE_DIR
git stash
git pull
git	log --pretty=format:"%h - %an, %ai:%ar : %s" -8
#
#
# copy deploy script file
cp	$SOURCE_DIR/src/main/resources-$PROFILE/$DEPLOY_SCRIPT_FILE_NAME	$DEPLOY_DIR
#
#
cd	$DEPLOY_DIR
chmod	a+x $DEPLOY_SCRIPT_FILE_NAME
bash $DEPLOY_SCRIPT_FILE_NAME
#