#! /bin/bash
# Run this script to kill the worker for testing fault tolerance.
for i in "$@"; do
            lsof -i udp:"$i" | grep -v PID | awk '{print $2}' | xargs kill -9
            if [ $? == 0 ]; then
            echo "killed $i"
            else
              echo "Failed to kill $i"
            fi
        done
