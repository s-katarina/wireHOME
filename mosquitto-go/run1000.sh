#!/bin/bash

for ((i=1;i<=1000;i++))
do 
  docker run --rm --network testnet --detach gomqtt
done
