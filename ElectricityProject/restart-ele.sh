#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
JAR_NAME=ElectricityProject-0.0.1-SNAPSHOT.jar
JAR_PATH="$APP_DIR/$JAR_NAME"
LOG_FILE="$APP_DIR/application.log"

if [[ ! -f "$JAR_PATH" ]]; then
    echo "错误：找不到 JAR 文件：$JAR_PATH" >&2
    exit 1
fi
if ! command -v java >/dev/null 2>&1; then
    echo "错误：找不到 java，请先安装 Java 并配置 PATH。" >&2
    exit 1
fi
if [[ ! -w "$APP_DIR" ]] || { [[ -e "$LOG_FILE" ]] && [[ ! -w "$LOG_FILE" ]]; }; then
    echo "错误：无法写入日志：$LOG_FILE" >&2
    exit 1
fi

# Read /proc arguments individually so that spaces in paths do not affect matching.
pids=()
for cmdline in /proc/[0-9]*/cmdline; do
    [[ -r "$cmdline" ]] || continue
    args=()
    mapfile -d '' -t args < "$cmdline" 2>/dev/null || continue
    (( ${#args[@]} > 0 )) || continue
    [[ ${args[0]##*/} == java ]] || continue
    for arg in "${args[@]:1}"; do
        if [[ "$arg" == -jar ]]; then
            pid=${cmdline#/proc/}
            pids+=("${pid%/cmdline}")
            break
        fi
    done
done

if (( ${#pids[@]} )); then
    echo "正在停止 java -jar 进程：${pids[*]}"
    for pid in "${pids[@]}"; do
        if ! kill -TERM "$pid" 2>/dev/null && kill -0 "$pid" 2>/dev/null; then
            echo "错误：无法停止进程 $pid，请检查权限。" >&2
            exit 1
        fi
    done
    for ((i=0; i<30; i++)); do
        remaining=()
        for pid in "${pids[@]}"; do
            kill -0 "$pid" 2>/dev/null && remaining+=("$pid")
        done
        (( ${#remaining[@]} == 0 )) && break
        sleep 1
    done
    if (( ${#remaining[@]} )); then
        echo "错误：进程在 30 秒内未退出：${remaining[*]}，未启动新程序。" >&2
        exit 1
    fi
else
    echo "没有发现运行中的 java -jar 进程。"
fi

cd "$APP_DIR"
nohup java -jar "$JAR_PATH" >> "$LOG_FILE" 2>&1 < /dev/null &
new_pid=$!
sleep 2
if ! kill -0 "$new_pid" 2>/dev/null; then
    echo "错误：程序启动后立即退出，请查看日志：$LOG_FILE" >&2
    exit 1
fi
echo "程序已启动，PID：$new_pid"
echo "日志：$LOG_FILE"
