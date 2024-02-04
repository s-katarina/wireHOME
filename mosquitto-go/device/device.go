package device

import (
	"encoding/json"
	"fmt"
	"strconv"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

type CommonBehavior interface {
	SendHeartBeat(client mqtt.Client)
	TurnOn(client mqtt.Client, topic string)
	TurnOff(client mqtt.Client, topic string)
	TakesElectisity(client mqtt.Client)
}

type BaseDevice struct {
	Id int
	ModelName string
	State bool
	UsesElectricity bool
	ConsumptionAmount float64
	PropertyId int
	On bool
	DeviceType string
}


type MessageDTO struct {
	DeviceId int	`json:"deviceId"`
	UsedFor string 	`json:"usedFor"`
	TimeStamp time.Time	`json:"timeStamp"`
}

type ElectisityDTO struct {
	DeviceId int	`json:"deviceId"`
	ConsumptionAmount float64 	`json:"consumptionAmount"`
	TimeStamp time.Time	`json:"timeStamp"`
}

func (device BaseDevice) SendHeartBeat(client mqtt.Client) {
	// if (!device.State){
	// 	return
	// }
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
		// fmt.Println("jbg")
        // text := fmt.Sprintf("Heartbeat %v", currentTime)
        token := client.Publish("heartbeat", 0, false, jsonData)
        token.Wait()
        time.Sleep(time.Second * 15)
    }
}

func OnObj(device BaseDevice) MessageDTO{
    currentTime:= time.Now()
	if (device.On) {
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		device.On = true
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "ON",
			TimeStamp: currentTime,
		}
}

func OffObj(device BaseDevice) MessageDTO{
    currentTime:= time.Now()
	if (!device.On) {
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		device.On = false
		return MessageDTO{
			DeviceId:   device.Id,
			UsedFor: "OFF",
			TimeStamp: currentTime,
		}
}

func (device BaseDevice) TurnOn(client mqtt.Client, topic string) bool{
	myObj := OnObj(device)
	fmt.Println(myObj)
	
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
func (device BaseDevice) TakesElectisity(client mqtt.Client) {
	if (!device.UsesElectricity){
			return
		}
	topic := fmt.Sprintf("energy/%d/%s", device.Id, "any-device")
    for {
		data := fmt.Sprintf("energy-maintaining,device-id=%d,property-id=%d,device-type=%s value=%f", device.Id, device.PropertyId, device.DeviceType, -device.ConsumptionAmount/6)
		token := client.Publish(topic, 0, false, data)
		token.Wait()
        time.Sleep(time.Second * 15)
    }
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