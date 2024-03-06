# wireHOME 💡 - Smart Home Monitoring
   ### Project for course Advanced Web Technologies
Application designed for monitoring and controlling smart devices inside smart properties.
      
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
   - Run command `mosquitto_passwd -c <password file> <username>` (example: `mosquitto_passwd -c pwfile client`) and insert password when asked (in this example password is `1234` and username `admin`)
   - After that check if file with credentials is generated (in this example it is `brokerPassFile`)
   - Run Mosquitto broker ((re)start service or start stand-alone application)
   - Set required information in **application.properties** (`mqtt.host`, `mqtt.port`, `mqtt.username` and `mqtt.password`)
   - Run mosquitto with command `mosquitto -c -v <cofiguration-file>`
   - Optionaly you can use the config files provided in folder `config files\mosquitto`
4. Install InfluxDB and Telegraf and set telegraf.config file the same as the config file provided
   - Open cmd and position inside directory where InfluxDB is installed (on Windows machine default installation directory is `C:\Program Files\influxdata\InfluxDB`) and run `influxd.exe`
   - Open cmd and position inside directory where Telegraf is installed (on Windows machine default installation directory is `C:\Program Files\influxdata\Telegraf`) and run `telegraf --config telegraf.conf` or 'telegraf.exe'
   - You can use the config files provided in folder `config files\telegraf`
5. Download NGINX and start it
   - You can use the config files provided in folder `config files\nginx` and place them in `nginx-1.24.0\conf`folder where you dowloaded NGINX.
   - Start NGINX by double clicking nginx.exe inside your nginx-1.24 folder 
6. Load dependencies of backend (`pom.xml`) with Maven and start application
7. Set your self in front folder. Run `npm install` and start application with `ng serve`. And go to localhost
8. mosquitto-go is ran by command `go run main.go`
9. Run applications in following order:
   1) Mosquitto broker
   2) PostgreSQL database
   3) NGINX
   4) InfluxDB
   5) Bakend application
   6) Front application
   7) Telegraf
   8) mosquitto-go (one or more of them)


## Reports
Reports are given for load and performace testing and divided into three parts for each team member. Testing was done using Locust.
* Member 1 report and scripts for testing are in the `reports1` folder
* Member 2 report and scripts for testing are in the `reports2` folder
* Member 3 report and scripts for testing are in the `reports3` folder


## Authors
* Milos Stojanovic ([github link](https://github.com/miloss01))
* Selena Milutin ([github link](https://github.com/SelenaMilutin))
* Katarina Spremic ([github link](https://github.com/s-katarina))


## Project status
Complete. 🎉
