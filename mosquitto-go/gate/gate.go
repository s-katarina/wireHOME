package gate

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

type Gate struct {
	device.BaseDevice
	IsPublic bool
	IsOpen   bool
}


func setToPublicRegime (gate Gate) device.MessageDTO {
    currentTime:= time.Now()
	if (gate.IsPublic) {
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		gate.IsPublic = true
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "PUBLIC",
			TimeStamp: currentTime,
		}
}

func setToPrivateRegime (gate Gate) device.MessageDTO{
    currentTime:= time.Now()
	if (!gate.IsPublic) {
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		gate.IsPublic = false
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "PRIVATE",
			TimeStamp: currentTime,
		}
}


func setToOpen (gate Gate) device.MessageDTO {
    currentTime:= time.Now()
	if (gate.IsOpen) {
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		gate.IsOpen = true
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "OPEN",
			TimeStamp: currentTime,
		}
}

func setToClosed (gate Gate) device.MessageDTO{
    currentTime:= time.Now()
	if (!gate.IsOpen) {
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		gate.IsOpen = false
		return device.MessageDTO{
			DeviceId:   gate.Id,
			UsedFor: "CLOSE",
			TimeStamp: currentTime,
		}
}


func (gate Gate) SubToRegimeSet(client mqtt.Client) {
	topic := fmt.Sprintf("%d/%s", gate.Id, "regime/set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}

func (gate Gate) SubToOpenSet(client mqtt.Client) {
	topic := fmt.Sprintf("%d/%s", gate.Id, "open/set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}

func getGate(deviceId int) Gate {

	apiUrl := fmt.Sprintf("%s/gate/%d", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return Gate{}
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return Gate{}
	}

	var sensorData = Gate{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return Gate{}
	}

	fmt.Println("Response for GET Lamp:", string(body))
	return sensorData

}

var gate Gate = getGate(9)

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternOn := "\\d+"
	patternRegime := "\\d+/regime/set" // \\d+ matches one or more digits
	patternOpen := "\\d+/open/set"

	if helper.IsTopicMatch(patternOn, msg.Topic()) {
		if string(msg.Payload()) == "ON" {
			changed := gate.TurnOn(client, "ON")
			if changed {
				gate.State = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := gate.TurnOff(client, "OFF")
			if changed {
				gate.State = true
			}
		}
	}

	if helper.IsTopicMatch(patternRegime, msg.Topic()) {
		fmt.Println("Topic is for Regime set")

		var public bool
		if string(msg.Payload()) == "PUBLIC" {
			public = true
		} else if string(msg.Payload()) == "PRIVATE" {
			public = false
		}
		changed := gate.ChangeRegime(client, fmt.Sprintf("%d/%s", gate.Id, "regime"), public)
		// Command can be executed, is sent to backend
		if changed {
			gate.IsPublic = public
		}
	}

	if helper.IsTopicMatch(patternOpen, msg.Topic()) {
		fmt.Println("Topic is for Open set")

		var open bool
		if string(msg.Payload()) == "OPEN" {
			open = true
		} else if string(msg.Payload()) == "CLOSE" {
			open = false
		}
		changed := gate.ChangeOpen(client, fmt.Sprintf("%d/%s", gate.Id, "open"), open)
		// Command can be executed, is sent to backend
		if changed {
			gate.IsOpen = open
		}
	}

}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Printf("Connect lost: %v", err)
}

func RunGate() {

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
	opts.SetUsername(constants.Username)
	opts.SetPassword(constants.Password)

	opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	messageDTO := device.MessageDTO{
		DeviceId:  gate.Id,
		UsedFor:   "Kill",
		TimeStamp: time.Now(),
	}
	jsonData, err := json.Marshal(messageDTO)
	if err != nil {
		fmt.Println("JSON convert error - will")
	}
	opts.SetWill("KILLED", string(jsonData), 1, false)

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}

	gate.Sub(client)
	gate.SubToRegimeSet(client)
	gate.SubToOpenSet(client)
	go pubDistanceSensorValue(client)
	gate.SendHeartBeat(client)
}

func pubDistanceSensorValue(client mqtt.Client) {
	topic := fmt.Sprintf("%d/%s", gate.Id, "distance-sensor")
	fmt.Println("Topic for pub " + topic)
}


func (gate Gate) ChangeRegime(client mqtt.Client, topic string, public bool) bool{
	var messageDTO device.MessageDTO
	if public {
		messageDTO = setToPublicRegime(gate)
	} else {
		messageDTO = setToPrivateRegime(gate)
	}
	
	jsonData, err := json.Marshal(messageDTO)
	if err != nil {
		fmt.Println("JSON convert error - gate regime")
	}
	fmt.Println(messageDTO)
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
	return messageDTO.UsedFor != "Error"
}

func (gate Gate) ChangeOpen(client mqtt.Client, topic string, open bool) bool {
	var messageDTO device.MessageDTO
	if open {
		messageDTO = setToOpen(gate)
	} else {
		messageDTO = setToClosed(gate)
	}
	
	jsonData, err := json.Marshal(messageDTO)
	if err != nil {
		fmt.Println("JSON convert error - gate open")
	}
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
	return messageDTO.UsedFor != "Error"
}
