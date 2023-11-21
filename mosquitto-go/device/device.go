package device

import (
	"encoding/json"
	"fmt"
	"strconv"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

type CommonBehavior interface {
	SendHeartBeat(client mqtt.Client, topic string)
	TurnOn(client mqtt.Client, topic string)
	TurnOff(client mqtt.Client, topic string)
}

type BaseDevice struct {
	Id int
	ModelName string
	State bool
	UsesElectricity bool
	ConsumptionAmount float64
	PropertyId int
}


type MessageDTO struct {
	DeviceId int	`json:"deviceId"`
	UsedFor string 	`json:"usedFor"`
	TimeStamp time.Time	`json:"timeStamp"`
}

func (device BaseDevice) SendHeartBeat(client mqtt.Client) {
	if (!device.State){
		return
	}
    for {
		currentTime:= time.Now()
		myObj := MessageDTO{
			DeviceId:   device.Id,
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
        token := client.Publish("heartbeat", 0, false, jsonData)
        token.Wait()
        time.Sleep(time.Second * 25)
    }
}

func OnObj(device BaseDevice) MessageDTO{
    currentTime:= time.Now()
	if (device.State) {
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		device.State = true
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "ON",
			TimeStamp: currentTime,
		}
}

func OffObj(device BaseDevice) MessageDTO{
    currentTime:= time.Now()
	if (!device.State) {
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		device.State = false
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "OFF",
			TimeStamp: currentTime,
		}
}

func (device BaseDevice) TurnOn(client mqtt.Client, topic string) bool{
	myObj := OnObj(device)
	
		// Convert the object to JSON
	jsonData, err := json.Marshal(myObj)
	if err != nil {
		// log.Fatal(err)
		fmt.Println("jbg")
	}
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
	return myObj.UsedFor == "ON"
}

func (device BaseDevice) TurnOff(client mqtt.Client, topic string) bool {
	myObj := OffObj(device)
	
		// Convert the object to JSON
	jsonData, err := json.Marshal(myObj)
	if err != nil {
		// log.Fatal(err)
		fmt.Println("jbg")
	}
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
	return myObj.UsedFor == "OFF"
}

func (device BaseDevice)Sub(client mqtt.Client) {
    topic := strconv.Itoa(device.Id)
    token := client.Subscribe(topic, 1, nil)
    token.Wait()
  fmt.Printf("Subscribed to topic: %s", topic)
}

func main() {
	fmt.Println("ok")
    
}