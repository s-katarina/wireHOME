package main

import (
	// "fmt"
	"sync"
	battery "tim10/mqtt/battery"
	// "tim10/mqtt/gate"
	// lamp "tim10/mqtt/lamp"
	solarPanel "tim10/mqtt/solarPanel"
	// ambientSensor "tim10/mqtt/ambientSensor"
	// airConditioner "tim10/mqtt/airConditioner"
	charger "tim10/mqtt/charger"
)

// ambientSensore "tim10/mqtt/ambientSensore"
// "tim10/mqtt/gate"

func main() {
	var wg sync.WaitGroup

	// Add the number of goroutines you want to wait for
	wg.Add(5)
	// go gate.RunGate()
	// go lamp.RunLamp()
	go battery.RunBattery(7)
	go solarPanel.RunSolarPanel(1);
	go solarPanel.RunSolarPanel(8);
	// go airConditioner.RunAirConditioner()
	// go ambientSensor.RunAmbientSensor();
	go charger.RunCharger(6)
	go charger.RunCharger(9)
	wg.Wait()

	// fmt.Println("All scripts completed")
}
