package main

import (
    solarPanel "tim10/mqtt/solarPanel"
	// ambientSensore "tim10/mqtt/ambientSensore"
	// "tim10/mqtt/gate"
	lamp "tim10/mqtt/lamp"
)


func main() {
    // ambientSensore.RunAmbientSensore();
    go solarPanel.RunSolarPanel();

    //gate.RunGate()
    lamp.RunLamp()
}


