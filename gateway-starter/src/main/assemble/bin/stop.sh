#!/bin/sh

cd `dirname $0`
. ./setenv.sh

# it's only for publish ,remember don't use it in normal model
# stop the server first


. ./server-ctl.sh stop

## clean the jar file
rm -rf *
