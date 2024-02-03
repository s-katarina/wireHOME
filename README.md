# project nwt2023



## Smart Home Monitoring
Aplication desinged for monitoring and manipulating smart devices inside of your smart homes or appartmants.


## Requirements
* Java 17
* Node.js and npm
* Angular CLI
* Maven
* [PostgreSQL database](https://www.postgresql.org/) listening on port 5432
* [Mosquitto MQTT broker](https://mosquitto.org/) listening on port 1883
* [NGINX: Advanced Load Balancer, Web Server, & Reverse Proxy](https://nginx.org/en/docs/windows.html)
* [InfluxDB v2](https://www.influxdata.com/downloads/) listening on port 8086
* [Telegraf](https://www.influxdata.com/downloads/) 


## Setup

1. Install Java and Maven
2. Install and start PostgreSQL
   - Set credentials for authentication in application.properties (`spring.datasource.username` and `spring.datasource.password`) and create database with name `projectnwt2023`
3. Install Mosquitto, add credentials and start it
   - Open cmd (it could require running it as Administrator) and position inside directory where Mosquitto is installed (on Windows machine default installation directory is `C:\Program Files\mosquitto`) 
   - Run command `mosquitto_passwd -c <password file> <username>` (example: `mosquitto_passwd -c pwfile client`) and insert password when asked (in this example password is `password`)
   - After that check if file with credentials is generated (in this example it is `pwfile`)
   - Run Mosquitto broker ((re)start service or start stand-alone application)
   - Set required information in **application.properties** (`mqtt.host`, `mqtt.port`, `mqtt.username` and `mqtt.password`)
   - Run mosquitto with command `mosquitto -c -v <cofiguration-file>`
4. Install InfluxDB and Telegraf and set telegraf.config file the same as the config file provided
   - Open cmd and position inside directory where InfluxDB is installed (on Windows machine default installation directory is `C:\Program Files\influxdata\InfluxDB`) and run `influxd.exe`
   - Open cmd and position inside directory where Telegraf is installed (on Windows machine default installation directory is `C:\Program Files\influxdata\Telegraf`) and run `telegraf --config telegraf.conf` or 'telegraf.exe'
5. Load dependencies of backend (`pom.xml`) with Maven and start application
6. Set your self in front folder. Run `npm install` and start application with `ng serve`
7. mosquitto-go is ran by command `go run main.go`
8. Run applications in following order:
   1) Mosquitto broker
   2) PostgreSQL database
   3) Bakend application
   4) Front application
   5) InfluxDB
   6) Telegraf
   7) mosquitto-go (one or more of them)


## Authors
* [Katarina Spremić]()
* [Miloš Stojanović]()
* [Selena Milutin]()


## Project status
Complete. 🎉
