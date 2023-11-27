package main

import (
    // "fmt"
    // mqtt "github.com/eclipse/paho.mqtt.golang"
    // // "log"
    // // "time"
    // ambientSensore "tim10/mqtt/ambientSensore"
    lamp "tim10/mqtt/lamp"
)


func main() {
    // ambientSensore.RunAmbientSensore()

    lamp1 := lamp.GetLamp(8)
    lamp.InitConnections(lamp1)
}


