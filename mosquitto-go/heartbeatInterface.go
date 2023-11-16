package main

import (
    "fmt"
    mqtt "github.com/eclipse/paho.mqtt.golang"
    "time"
	"encoding/json"
)


type CommonBehavior interface {
	SendHeartBeat(client mqtt.Client, topic string)
	TurnOn(client mqtt.Client, topic string)
	TurnOff(client mqtt.Client, topic string)
}

type BaseDevice struct {
	id int
	modelName string
	active bool
	usesElectricity bool
	consumptionAmount float64
	topic string
}

type AmbientSensore struct {
	BaseDevice
}

type AirConditioner struct {
	BaseDevice
	temp int
}

type MessageDTO struct {
	DeviceId int	`json:"deviceId"`
	UsedFor string 	`json:"usedFor"`
	TimeStamp time.Time	`json:"timeStamp"`
}

func (device BaseDevice) SendHeartBeat(client mqtt.Client, topic string) {
    for {
		currentTime:= time.Now()
		myObj := MessageDTO{
			DeviceId:   device.id,
			UsedFor: "Heartbeat",
			TimeStamp: currentTime,
		}
	
		// Convert the object to JSON
		jsonData, err := json.Marshal(myObj)
		if err != nil {
			// log.Fatal(err)
			fmt.Println("jbg")
		}
	
        // text := fmt.Sprintf("Heartbeat %v", currentTime)
        token := client.Publish(topic, 0, false, jsonData)
        token.Wait()
        time.Sleep(time.Second)
    }
}

func (device BaseDevice) TurnOn(client mqtt.Client, topic string) {
	
    currentTime:= time.Now()
		myObj := MessageDTO{
			DeviceId:   device.id,
			UsedFor: "TurnOn",
			TimeStamp: currentTime,
		}
	
		// Convert the object to JSON
		jsonData, err := json.Marshal(myObj)
		if err != nil {
			// log.Fatal(err)
			fmt.Println("jbg")
		}
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
    time.Sleep(time.Second)

}

func (device BaseDevice) TurnOff(client mqtt.Client, topic string) {
    currentTime:= time.Now()
		myObj := MessageDTO{
			DeviceId:   device.id,
			UsedFor: "TurnOff",
			TimeStamp: currentTime,
		}
	
		// Convert the object to JSON
		jsonData, err := json.Marshal(myObj)
		if err != nil {
			// log.Fatal(err)
			fmt.Println("jbg")
		}
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
    time.Sleep(time.Second)

}

func lala() {
    ambientSensor := AmbientSensore{
		BaseDevice: BaseDevice{id: 1, modelName: "nana", active: false, usesElectricity: true, consumptionAmount: 232.0, topic: "lala"},
	}
	airConditioner := AirConditioner{
		BaseDevice: BaseDevice{id: 3, modelName: "aaa", active: false, usesElectricity: true, consumptionAmount: 232.0, topic: "jajaj"},
		temp: 8,
	}
	fmt.Printf("amb ID: %v, Name: %s\n", ambientSensor.id, ambientSensor.modelName)
	fmt.Printf("Sprinkler ID: %v, Name: %s\n", airConditioner.id, airConditioner.modelName)

	// Accessing specific fields of Gate and Sprinkler
	fmt.Printf("Is Automatic: %v\n", airConditioner.temp)
	// airConditioner.BaseDevice.SendHeartBeat()
	// ambientSensor.BaseDevice.TurnOn()
}