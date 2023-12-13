package gate

import (
	"encoding/json"
	"fmt"
	"io"
	"math"
	"math/rand"
	"net/http"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

type Gate struct {
	device.BaseDevice
	IsPublic             bool `json:"public"`
	IsOpen               bool `json:"open"`
	AllowedLicencePlates []string
}

type Caller string

const (
	User      Caller = "USER"
	GateEvent Caller = "GATE_EVENT"
)

type GateEventMessageDTO struct {
	device.MessageDTO
	Caller string `json:"caller"`
}

func setToPublicRegime(gate Gate) device.MessageDTO {
	currentTime := time.Now()
	if gate.IsPublic {
		return device.MessageDTO{
			DeviceId:  gate.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	gate.IsPublic = true
	return device.MessageDTO{
		DeviceId:  gate.Id,
		UsedFor:   "PUBLIC",
		TimeStamp: currentTime,
	}
}

func setToPrivateRegime(gate Gate) device.MessageDTO {
	currentTime := time.Now()
	if !gate.IsPublic {
		return device.MessageDTO{
			DeviceId:  gate.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	gate.IsPublic = false
	return device.MessageDTO{
		DeviceId:  gate.Id,
		UsedFor:   "PRIVATE",
		TimeStamp: currentTime,
	}
}

func (gate Gate) setToOpen(caller Caller) GateEventMessageDTO {
	fmt.Printf("IN SET TO OPEN, before set: %t\n", gate.IsOpen)
	
	currentTime := time.Now()
	if gate.IsOpen {
		message := device.MessageDTO{
			DeviceId:  gate.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
		return GateEventMessageDTO{
			MessageDTO: message,
			Caller:     string(caller),
		}
	}
	gate.IsOpen = true
	fmt.Printf("IN SET TO OPEN, after set: %t\n", gate.IsOpen)
	message := device.MessageDTO{
		DeviceId:  gate.Id,
		UsedFor:   "OPEN",
		TimeStamp: currentTime,
	}
	return GateEventMessageDTO{
		MessageDTO: message,
		Caller:     string(caller),
	}
}

func (gate Gate) setToClosed(caller Caller) GateEventMessageDTO {
	fmt.Printf("IN SET TO CLOSED, before set: %t\n", gate.IsOpen)
	currentTime := time.Now()
	if !gate.IsOpen {
		message := device.MessageDTO{
			DeviceId:  gate.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
		return GateEventMessageDTO{
			MessageDTO: message,
			Caller:     string(caller),
		}
	}
	gate.IsOpen = false
	fmt.Printf("IN SET TO CLOSED, after set: %t\n", gate.IsOpen)
	message := device.MessageDTO{
		DeviceId:  gate.Id,
		UsedFor:   "CLOSE",
		TimeStamp: currentTime,
	}
	return GateEventMessageDTO{
		MessageDTO: message,
		Caller:     string(caller),
	}
}

func (gate Gate) SubToRegimeSet(client mqtt.Client) {
	topic := fmt.Sprintf("gate/%d/%s", gate.Id, "regime/set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}

func (gate Gate) SubToOpenSet(client mqtt.Client) {
	topic := fmt.Sprintf("gate/%d/%s", gate.Id, "open/set")
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

		var public bool
		if string(msg.Payload()) == "PUBLIC" {
			public = true
		} else if string(msg.Payload()) == "PRIVATE" {
			public = false
		}
		changed := gate.ChangeRegime(client, fmt.Sprintf("gate/%d/%s", gate.Id, "regime"), public)
		// Command can be executed, is sent to backend
		if changed {
			gate.IsPublic = public
		}
	}

	if helper.IsTopicMatch(patternOpen, msg.Topic()) {

		var open bool
		if string(msg.Payload()) == "OPEN" {
			open = true
		} else if string(msg.Payload()) == "CLOSE" {
			open = false
		}
		changed := gate.ChangeOpen(client, open, User)
		// Command can be executed, is sent to backend
		if changed {
			gate.IsOpen = open
		}
		fmt.Println("Gate open by User, after response", gate.IsOpen)

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
	go simulateGate(client)
	gate.SendHeartBeat(client)
}

func pubDistanceSensorValue(client mqtt.Client) {
	topic := fmt.Sprintf("gate/%d/%s", gate.Id, "distance-sensor")
	fmt.Println("Topic for pub " + topic)
}

func (gate Gate) ChangeRegime(client mqtt.Client, topic string, public bool) bool {
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

func (gate Gate) ChangeOpen(client mqtt.Client, open bool, caller Caller) bool {
	fmt.Printf("IN CHANGE OPEN, before call: %t, wanted to be %t\n", gate.IsOpen, open)

	topic := fmt.Sprintf("gate/%d/%s", gate.Id, "open")
	var messageDTO GateEventMessageDTO
	if open {
		messageDTO = gate.setToOpen(caller)
	} else {
		messageDTO = gate.setToClosed(caller)
	}

	fmt.Printf("IN CHANGE OPEN, after call: %t, should be %t\n", gate.IsOpen, open)

	jsonData, err := json.Marshal(messageDTO)
	if err != nil {
		fmt.Println("JSON convert error - gate open")
	}
	token := client.Publish(topic, 0, false, jsonData)
	token.Wait()
	return messageDTO.UsedFor != "Error"
}

func simulateProximitySensorRead() float64 {

	now := time.Now()
	hour := float64(now.Hour()) + float64(now.Minute())/60.0

	// Higher probability of vehicle showing up from 07h to 23h
	detectedScore := (0.5*math.Sin((hour-7.0)*math.Pi/16) + rand.Float64()) * float64(rand.Intn(2))

	return detectedScore

}

func simulateLicencePlateRead() string {

	chars := "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	nums := "0123456789"

	// 60% chance that vehicle is with allowed licence plate
	if rand.Intn(101) < 60 && len(gate.AllowedLicencePlates) > 0 {
		return gate.AllowedLicencePlates[rand.Intn(len(gate.AllowedLicencePlates))]
	}

	i := 0
	licenceChars := ""
	licenceNums := ""
	for i < 3 {
		if i < 2 {
			licenceChars = licenceChars + string(chars[rand.Intn(len(chars))])
		}
		licenceNums = licenceNums + string(nums[rand.Intn(len(nums))])
		i += 1
	}
	return licenceChars + licenceNums
}

func simulateGate(client mqtt.Client) {

	for {
		detectedScore := simulateProximitySensorRead()

		fmt.Println("Proximity detectedScore ", detectedScore)
		// Vehicle is detected
		if detectedScore > 0.7 {
			fmt.Println("Gate regime ", gate.IsPublic)
			fmt.Println("Gate open ", gate.IsOpen)

			// Read licence plate
			licencePlate := simulateLicencePlateRead()
			fmt.Println("Licence plate ", licencePlate)

			entrance := true
			if rand.Intn(2) == 0 {
				entrance = false
			}
			fmt.Println("Gate event type entrance is ", entrance)

			processVehicleEvent(client, licencePlate, entrance)

		}

		time.Sleep(time.Second * constants.ProximitySensorReadPeriod)
	}
}

func processVehicleEvent(client mqtt.Client, licencePlate string, entrance bool) {

	// Gate is in PRIVATE regime and licence plate is not among the allowed
	if !gate.IsPublic && !containsLicencePlate(licencePlate) {
		fmt.Println("Vehicle not allowed")
		return
	}

	if !gate.IsOpen {
		if (gate.ChangeOpen(client, true, GateEvent)) {
			gate.IsOpen = true
		}
		time.Sleep(time.Second)
		fmt.Println("Gate open after sending to open GATE_EVENT ", gate.IsOpen)
	}
	time.Sleep(time.Second * 30)
	if (gate.ChangeOpen(client, false, GateEvent)) {
		gate.IsOpen = false
	}
	fmt.Println("Gate open after sending to close GATE_EVENT ", gate.IsOpen)


	// Publish event type (enterance or leaving), vehicle licence plate, timestamp

}

func containsLicencePlate(licencePlate string) bool {
	for _, element := range gate.AllowedLicencePlates {
		if element == licencePlate {
			return true
		}
	}
	return false
}
