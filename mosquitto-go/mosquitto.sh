#/bin/bash
cd C:
cd "C:\Program Files\mosquitto"
# pause
mosquitto -c brokerConf.conf
cmd /k

# cmd /k cd C: & cd "C:\Program Files\mosquitto" & mosquitto -v -c brokerConf.conf
