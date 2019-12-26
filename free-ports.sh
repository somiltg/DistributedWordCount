#! /bin/bash
# Run this script after you have killed the process as the ports will be bound to the process. Use argument as the number of workers.
# Currently runs before the main execution by the master program itself. No need to run manually.
for i in $(seq 1 "$1"); do
            lsof -i udp:$((12000+i-1)) | grep -v PID | awk '{print $2}' | xargs kill -9
            if [ $? == 0 ]; then
            echo "killed "$((12000+i-1))
            fi
        done
 lsof -i udp:11999 | grep -v PID | awk '{print $2}' | xargs kill -9
 if [ $? == 0 ]; then
            echo "killed 11999"
fi
