#!/usr/bin/env bash

REPOSITORY=/home/ubuntu/

if [ -d $REPOSITORY/app ]; then
    rm -rf $REPOSITORY/app
fi
mkdir -vp $REPOSITORY/app