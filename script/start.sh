#!/bin/sh
########################################################
# 采用 systemd 启动服务                                  #
########################################################
# start：  /www/server/script/start.sh 服务名 startd    #
# restart：/www/server/script/start.sh 服务名 restartd  #
# stop：   /www/server/script/start.sh 服务名 stopd     #
########################################################

# check service name
if [ -z $1 ]; then
    echo "No service name args."
    exit 1
fi

SERVER_NAME=$1
JAR_NAME="${SERVER_NAME}.jar"
PROFILES_ACTIVE="prod"
SERVER_HOME="/www/server/${SERVER_NAME}"
LOGGER_HOME="${SERVER_HOME}/logs"
SYSTEMD_NAME="${SERVER_NAME}.service"
SCRIPT_SYSTEMD="${SERVER_HOME}/${SYSTEMD_NAME}"

# log dir
if [ ! -d $LOGGER_HOME ]; then
    mkdir -p $LOGGER_HOME
    echo "log dir LOGGER_HOME=$LOGGER_HOME"
fi

# set java home 适用于服务器上默认 jdk 不支持的情况
JAVA_HOME1="/www/server/jdk8/"
JAVA_HOME2="/usr/local/jdk"
JAVA_HOME3="/data/jdk"
# export
PATH="$JAVA_HOME1/bin/:$JAVA_HOME2/bin/:$JAVA_HOME3/bin/:$PATH"

# jvm args
[ -z "$JAVA_OPTS" ] && JAVA_OPTS=" -Xms1024M -Xmx1024M -Xmn512M -Xss256k "

SERVER_OPTS=" ${JAVA_OPTS} \
-XX:SurvivorRatio=8 \
-XX:+UseConcMarkSweepGC \
-XX:MaxTenuringThreshold=5 \
-XX:GCTimeRatio=19 \
-XX:CMSInitiatingOccupancyFraction=85 \
-XX:CMSFullGCsBeforeCompaction=1 \
-XX:+PrintTenuringDistribution \
-XX:+PrintCommandLineFlags \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-Xloggc:${LOGGER_HOME}/gc-%t.log \
-XX:-OmitStackTraceInFastThrow \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=${LOGGER_HOME}/java_heapdump.hprof"

# generate systemd service file
function write_systemd() {
    cat >"${SCRIPT_SYSTEMD}" <<-EOF
[Unit]
Description=${SERVER_NAME}
After=network.target

[Service]
Type=forking
WorkingDirectory=/www/server/${SERVER_NAME}
ExecStart=/www/server/script/start.sh ${SERVER_NAME} start
ExecReload=/www/server/script/start.sh ${SERVER_NAME} restart
ExecStop=/www/server/script/start.sh ${SERVER_NAME} stop
PrivateTmp=true

[Install]
WantedBy=multi-user.target
EOF
}

# check systemd service file
function enable_systemd() {
    if [ ! -f "${SCRIPT_SYSTEMD}" ]; then
        echo "write systemd ${SYSTEMD_NAME}"
        systemctl disable "${SCRIPT_SYSTEMD}"
        write_systemd
        cp "${SCRIPT_SYSTEMD}" /etc/systemd/system
        systemctl daemon-reload
        systemctl enable "${SCRIPT_SYSTEMD}"
        echo "systemd enable $SYSTEMD_NAME"
    else
        echo "systemd file $SYSTEMD_NAME is enabled."
    fi
}

start_systemd() {
    enable_systemd
    echo "start $SYSTEMD_NAME"
    systemctl start "${SYSTEMD_NAME}"
    systemctl is-active --quiet "${SYSTEMD_NAME}" && echo "Service is running..."
}

restart_systemd() {
    enable_systemd
    echo "restart $SYSTEMD_NAME"
    systemctl restart "${SYSTEMD_NAME}"
    systemctl is-active --quiet "${SYSTEMD_NAME}" && echo "Service is running..."
}

stop_systemd() {
    enable_systemd
    echo "stop $SYSTEMD_NAME"
    systemctl stop "${SYSTEMD_NAME}"
}

# start
start() {
    eval exec "java -server $SERVER_OPTS \
        -jar $SERVER_HOME/$JAR_NAME \
        --spring.profiles.active=$PROFILES_ACTIVE \
        -DLOGGING_PATH=$LOGGER_HOME \
        -Duser.timezone=Asia/Shanghai \
        -Dfile.encoding=UTF-8 \
        -Djava.security.egd=file:/dev/./urandom \
        >> $LOGGER_HOME/nohup.log 2>&1 &"
    if [ $? -eq 0 ]; then
        sleep 1
        echo "Service：“$SERVER_NAME” start successful~~~"
    else
        echo "Service：“$SERVER_NAME” start failure."
        exit 2
    fi
}

# check service
check_service() {
    pid_list=($(ps -ef | grep $JAR_NAME | grep -v grep | awk '{print $2}'))
    ps_count=${#pid_list[@]}
    test $ps_count -gt 0 && { echo "Found $ps_count $JAR_NAME service"; }
    return $ps_count
}

# kill
stop() {
    sleep 1 && check_service
    [[ ${kill_retry} -le 3 ]] && kill_args='' || kill_args='-9'
    test $ps_count -gt 0 && {
        echo "Try to kill running service retry count ${ps_count}"
        kill $kill_args ${pid_list[@]}
        kill_result=$?
    } || {
        echo "Service $JAR_NAME not running"
        return 0
    }
    test "$kill_result" -ne 0 && { echo "Kill Failed,Skip..."; } || echo "Kill command send,Waiting for $JAR_NAME process quit"
    while [ $ps_count -ne 0 ]; do
        let kill_retry=$kill_retry+1
        stop
    done
}

# restart
restart() {
    if [ ! -f "${SERVER_HOME}/${JAR_NAME}" ]; then
        echo "Jar: “${SERVER_HOME}/${JAR_NAME}” not sexists start failure."
        exit 4
    fi
    stop
    start
}

case "$2" in
    start)
        start
        ;;
    restart)
        restart
        ;;
    stop)
        stop
        ;;
    startd)
        start_systemd
        ;;
    restartd)
        restart_systemd
        ;;
    stopd)
        stop_systemd
        ;;
    *)
    echo -e $"=== Run $0 Use arg:  { startd | restartd | stopd } ==="
    ;;
esac
exit $?
