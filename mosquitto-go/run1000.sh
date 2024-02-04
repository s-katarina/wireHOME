#!/bin/bash

cleanup() {
  taskkill -f -im main.exe
}
trap cleanup SIGINT

pids=()

go run main.go 6 &
pids+=($!)

for i in {1..50}; do
  go run main.go 7 &
  pids+=($!)
done

for pid in "${pids[@]}"; do
  wait $pid
done
