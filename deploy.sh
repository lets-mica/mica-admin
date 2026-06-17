#!/bin/sh
#####################################################
# 使用说明：                                          #
# 1. 用于linux 或者 mac 电脑快速发布服务                 #
# 2. 使用 sh deploy.sh                               #
#####################################################

#代码打包
echo "打包代码中..."
mvn clean package -Pprod -U -Dmaven.test.skip=true > /dev/null

#变量
SERVER_NAME="mica-admin-server"
SERVER_HOME="/www/server/${SERVER_NAME}"
echo "变量 SERVER_HOME=${SERVER_HOME}"

#上传代码
echo "tx 上传代码中..."
scp "./script/start.sh" tx:"/www/server/script"
scp "./${SERVER_NAME}/target/${SERVER_NAME}.jar" tx:"${SERVER_HOME}"

#重启
ssh tx "/www/server/script/start.sh ${SERVER_NAME} restartd"
echo "tx 启动完成"

#清理本地打包文件，避免引用 prod 的配置
echo "清理本地编译中..."
mvn clean package -Dmaven.test.skip=true > /dev/null
