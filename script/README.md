# 脚本说明

注意： 只支持 linux，脚本会自动创建 systemd 服务，服务器重启后会自动启动。

## 步骤
- 将 jar 包上传到 /www/server/${SERVER_NAME}/${SERVER_NAME}.jar
- 将 script/start.sh 脚本上传到 /www/server/script/start.sh

## 启动
- start：  /www/server/script/start.sh 服务名 startd
- restart：/www/server/script/start.sh 服务名 restartd
- stop：   /www/server/script/start.sh 服务名 stopd
