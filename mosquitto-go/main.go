package main

import (
	// "fmt"
	"sync"
	"tim10/mqtt/washingMachine"
)

func main() {
	var wg sync.WaitGroup

	// Add the number of goroutines you want to wait for
	wg.Add(1)
	// go gate.RunGate()
	// go lamp.RunLamp()
	// go battery.RunBattery(3)
	// go solarPanel.RunSolarPanel(4);
	//go airConditioner.RunAirConditioner();
	// go ambientSensor.RunAmbientSensor();
	go washingMachine.RunWashingMachine();
	wg.Wait()

	// fmt.Println("All scripts completed")
}
