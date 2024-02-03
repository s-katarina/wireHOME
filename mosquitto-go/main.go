package main

import (
	// "fmt"
	"fmt"
	"os"
	"strconv"
	"sync"

	// "tim10/mqtt/washingMachine"
	// battery "tim10/mqtt/battery"
	// "tim10/mqtt/gate"
	// solarPanel "tim10/mqtt/solarPanel"
	// ambientSensor "tim10/mqtt/ambientSensor"
	// airConditioner "tim10/mqtt/airConditioner"
	// charger "tim10/mqtt/charger"
	// airConditioner "tim10/mqtt/airConditioner"
	// ambientSensor "tim10/mqtt/ambientSensor"
	// battery "tim10/mqtt/battery"
	// "tim10/mqtt/gate"
	lamp "tim10/mqtt/lamp"
	// solarPanel "tim10/mqtt/solarPanel"
	// "tim10/mqtt/sprinkler"
	// "tim10/mqtt/airConditioner"
)

func main() {
	var wg sync.WaitGroup

	// Add the number of goroutines you want to wait for
	var deviceIdString = os.Args[1]
	var deviceId, _ = strconv.Atoi(deviceIdString)
	fmt.Println(deviceId)
	wg.Add(1)

	// lamp.SetLamp(17)
	lamp.SetLamp(2)
	go lamp.RunLamp()

	// gate.SetGate(16)
	// gate.SetGate(1)
	// go gate.RunGate()

	// sprinkler.SetSprinkler(11)
	// sprinkler.SetSprinkler(7)
	// go sprinkler.RunSprinkler()

	// go battery.RunBattery(7)
	// go battery.RunBattery(3);

	// go solarPanel.RunSolarPanel(1);

	// go solarPanel.RunSolarPanel(1);

	// go airConditioner.RunAirConditioner()
	// go ambientSensor.RunAmbientSensor();
	// go charger.RunCharger(6)
	// go charger.RunCharger(9)
	// go charger.RunCharger(6)
	// go gate.RunGate()
	// go lamp.RunLamp()
	// go battery.RunBattery(3)
	// go solarPanel.RunSolarPanel(4);
	// go airConditioner.RunAirConditioner()
	// go ambientSensor.RunAmbientSensor();
	// go gate.RunGate()
	// go battery.RunBattery(3)
	// go solarPanel.RunSolarPanel(4);
	// go airConditioner.RunAirConditioner();
	// go ambientSensor.RunAmbientSensor();
	// go washingMachine.RunWashingMachine();
	wg.Wait()

	// fmt.Println("All scripts completed")
}
