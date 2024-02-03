package main

import (
	// "fmt"
	"fmt"
	"os"
	"strconv"
	"sync"

	airConditioner "tim10/mqtt/airConditioner"
	ambientSensor "tim10/mqtt/ambientSensor"
	battery "tim10/mqtt/battery"
	charger "tim10/mqtt/charger"
	"tim10/mqtt/gate"
	lamp "tim10/mqtt/lamp"
	solarPanel "tim10/mqtt/solarPanel"
	"tim10/mqtt/sprinkler"
	"tim10/mqtt/washingMachine"
)

func main() {
	var wg sync.WaitGroup

	var deviceIdString = os.Args[1]
	var choice, _ = strconv.Atoi(deviceIdString)
	fmt.Println(choice)

	// Add the number of goroutines you want to wait for
	wg.Add(9)

	if choice == 0 {
		lamp.SetLamp(2)
		go lamp.RunLamp()
		gate.SetGate(1)
		go gate.RunGate()
		sprinkler.SetSprinkler(7)
		go sprinkler.RunSprinkler()
	
		go solarPanel.RunSolarPanel(4);
		go charger.RunCharger(21)
		go battery.RunBattery(3)
	
		go airConditioner.RunAirConditioner(6)
		go ambientSensor.RunAmbientSensor(5);
		go washingMachine.RunWashingMachine(20);
	}

	if choice == 1 {
		lamp.SetLamp(17)
		go lamp.RunLamp()
		gate.SetGate(16)
		go gate.RunGate()
		sprinkler.SetSprinkler(11)
		go sprinkler.RunSprinkler()
	
		go solarPanel.RunSolarPanel(18);
		go charger.RunCharger(12)
		go battery.RunBattery(19)
	
		go airConditioner.RunAirConditioner(14);
		go ambientSensor.RunAmbientSensor(13);
		go washingMachine.RunWashingMachine(15);
	}
	
	wg.Wait()

	// fmt.Println("All scripts completed")
}
