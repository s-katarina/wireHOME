package main

import (
	// "fmt"
	"sync"
	// "tim10/mqtt/washingMachine"
	// battery "tim10/mqtt/battery"
	// "tim10/mqtt/gate"
	// lamp "tim10/mqtt/lamp"
	// solarPanel "tim10/mqtt/solarPanel"
	// ambientSensor "tim10/mqtt/ambientSensor"
	// airConditioner "tim10/mqtt/airConditioner"
	charger "tim10/mqtt/charger"
	// airConditioner "tim10/mqtt/airConditioner"
	// ambientSensor "tim10/mqtt/ambientSensor"
	// battery "tim10/mqtt/battery"
	// "tim10/mqtt/gate"
	// solarPanel "tim10/mqtt/solarPanel"
	// "tim10/mqtt/sprinkler"
	// "tim10/mqtt/airConditioner"
)

func main() {
	var wg sync.WaitGroup

	// Add the number of goroutines you want to wait for
	wg.Add(2)
	// go gate.RunGate()
	// go lamp.RunLamp()
	// go battery.RunBattery(7)
	for i := 0; i < 80; i++ {
		go charger.RunCharger(6)
	}
	// go battery.RunBattery(3);

	// go solarPanel.RunSolarPanel(1);

	// go solarPanel.RunSolarPanel(1);

	// go airConditioner.RunAirConditioner()
	// go ambientSensor.RunAmbientSensor();
	// go charger.RunCharger(6)
	// go charger.RunCharger(6)
	// go gate.RunGate()
	// go lamp.RunLamp()
	// go battery.RunBattery(3)
	// go solarPanel.RunSolarPanel(4);
	// go airConditioner.RunAirConditioner()
	// go ambientSensor.RunAmbientSensor();
	// go sprinkler.RunSprinkler();
	// go gate.RunGate()
	// go lamp.RunLamp()
	// go battery.RunBattery(3)
	// go solarPanel.RunSolarPanel(4);
	// go airConditioner.RunAirConditioner();
	// go ambientSensor.RunAmbientSensor();
	// go washingMachine.RunWashingMachine();
	wg.Wait()

	// fmt.Println("All scripts completed")
}
