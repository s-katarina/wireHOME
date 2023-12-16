package ambientSensor

import (
	"encoding/json"
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"io"
	"log"
	"math/rand"
	"net/http"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"
)

type AmbientSensor struct {
	device.BaseDevice
	CurrentTemp float32 `json:"currentTemp"`
	CurrentHum float32 `json:"CurrentHum"`
	Client mqtt.Client
}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Printf("Connect lost: %v", err)
}

func getAmbientSensor(deviceId int) AmbientSensor {

	apiUrl := fmt.Sprintf("%s/ambientSensor/%d", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return AmbientSensor{}
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return AmbientSensor{}
	}

	var sensorData = AmbientSensor{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return AmbientSensor{}
	}

	fmt.Println("Response for GET Lamp:", string(body))
	fmt.Println(sensorData)
	return sensorData

}

var ambientSensor AmbientSensor = getAmbientSensor(2)

func SetAmbientSensor(id int) {
	ambientSensor = getAmbientSensor(id)
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternOn := "\\d+"

	if helper.IsTopicMatch(patternOn, msg.Topic()) {
		if string(msg.Payload()) == "ON" {
			changed := ambientSensor.TurnOn(client, "ON")
			if changed {
				ambientSensor.State = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := ambientSensor.TurnOff(client, "OFF")
			if changed {
				ambientSensor.State = false
			}
		}
	}
}

func RunAmbientSensor() {

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
	opts.SetUsername(constants.Username)
	opts.SetPassword(constants.Password)

	opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:  ambientSensor.Id,
		UsedFor:   "Kill",
		TimeStamp: time.Now(),
	}
	jsonData, err := json.Marshal(myObj)
	if err != nil {
		log.Fatal(err)
	}
	opts.SetWill("KILLED", string(jsonData), 1, false)

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(any(token.Error()))
	}
	ambientSensor.Client = client

	ambientSensor.Sub(client)
	go pubAmbientSensorValue(client)
	go ambientSensor.TakesElectisity(client)
	ambientSensor.SendHeartBeat(client)
}

func getTemp() float32 {
	var diff = rand.Intn(5 + 5) - 5
	return ambientSensor.CurrentTemp + float32(diff)
}

func getHum() float32 {
	var diff = rand.Intn(5 + 5) - 5
	return ambientSensor.CurrentHum + float32(diff)
}

func pubAmbientSensorValue(client mqtt.Client) {
	topicTemp := fmt.Sprintf("ambientSensor/%d/%s", ambientSensor.Id, "temp")
	topicHum := fmt.Sprintf("ambientSensor/%d/%s", ambientSensor.Id, "hum")
	fmt.Println("Topic for pub " + topicTemp)
	fmt.Println("Topic for pub " + topicHum)

	for {
		var temp = getTemp()
		var hum = getHum()

		dataTemp := fmt.Sprintf("temp,device-id=%d value=%f", ambientSensor.Id, temp)
		dataHum := fmt.Sprintf("hum,device-id=%d value=%f", ambientSensor.Id, hum)

		token := client.Publish(topicTemp, 0, false, dataTemp)
		token.Wait()
		if token.Error() != nil {
			log.Fatal(token.Error())
		}
		fmt.Println("Message from temp published successfully")

		token = client.Publish(topicHum, 0, false, dataHum)
		token.Wait()
		if token.Error() != nil {
			log.Fatal(token.Error())
		}
		fmt.Println("Message from hum published successfully")


		time.Sleep(time.Second * 3)
	}
}
