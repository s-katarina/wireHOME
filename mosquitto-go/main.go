package main

import (
    solarPanel "tim10/mqtt/solarPanel"
	// ambientSensore "tim10/mqtt/ambientSensore"
	// "tim10/mqtt/gate"
	lamp "tim10/mqtt/lamp"
    // battery "tim10/mqtt/battery"
)


func main() {
    // ambientSensore.RunAmbientSensore();
    // go battery.RunBattery(3)
    go solarPanel.RunSolarPanel(1);

    // gate.RunGate()
    lamp.RunLamp()
}


