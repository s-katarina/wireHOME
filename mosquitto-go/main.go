package main

import (
	// "fmt"
	"sync"
	// battery "tim10/mqtt/battery"
	// "tim10/mqtt/gate"
	// lamp "tim10/mqtt/lamp"
	// solarPanel "tim10/mqtt/solarPanel"
	// ambientSensor "tim10/mqtt/ambientSensor"
	airConditioner "tim10/mqtt/airConditioner"
)

// ambientSensore "tim10/mqtt/ambientSensore"
// "tim10/mqtt/gate"

func main() {
	var wg sync.WaitGroup

	// Add the number of goroutines you want to wait for
	wg.Add(1)
	// go gate.RunGate()
	// go lamp.RunLamp()
	// go battery.RunBattery(3)
	// go solarPanel.RunSolarPanel(4);
	go airConditioner.RunAirConditioner();
	// go ambientSensor.RunAmbientSensor();
	wg.Wait()

	// fmt.Println("All scripts completed")
}
