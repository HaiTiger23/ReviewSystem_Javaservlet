#!/bin/bash

# Doraemon auto-kill Tomcat cũ rồi khởi động Tomcat foreground cho Nobita nè!
./deploy.sh
TOMCAT_DIR="/opt/homebrew/Cellar/tomcat/11.0.6/libexec"

echo "Tìm & kill mọi tiến trình Tomcat/tail cũ (trừ chính script này)..."
MY_PID=$$
ps aux | grep -E 'tomcat|catalina|tail' | grep -v grep | awk -v mypid="$MY_PID" '$2 != mypid {print $2}' | xargs -r kill -9

echo "Khởi động Tomcat (foreground)..."
cd "$TOMCAT_DIR/bin"
./catalina.sh run