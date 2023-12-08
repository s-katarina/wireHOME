package lamp

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math"
	"math/rand"
	"net/http"
	"strconv"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

type Lamp struct {
	device.BaseDevice
	bulbState bool
	lightSensorValue int
	client mqtt.Client
}

const lightOnTreshold = 30000

func bulbOn(lamp Lamp) device.MessageDTO {
	currentTime := time.Now()
	if lamp.bulbState {
		return device.MessageDTO{
			DeviceId:  lamp.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	lamp.bulbState = true
	return device.MessageDTO{
		DeviceId:  lamp.Id,
		UsedFor:   "ON",
		TimeStamp: currentTime,
	}
}

func bulbOff(lamp Lamp) device.MessageDTO {
	currentTime := time.Now()
	if !lamp.bulbState {
		return device.MessageDTO{
			DeviceId:  lamp.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	lamp.bulbState = false
	return device.MessageDTO{
		DeviceId:  lamp.Id,
		UsedFor:   "OFF",
		TimeStamp: currentTime,
	}
}

func (lamp Lamp) SubToBulbSet(client mqtt.Client) {
	topic := fmt.Sprintf("%d/%s", lamp.Id, "bulb/set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}

type lightSensorValue struct {
	Id        string `json:"deviceId"`
	TimeStamp string `json:"timeStamp"`
	Val       string `json:"value"`
}

func getLamp(deviceId int) Lamp {

	apiUrl := fmt.Sprintf("%s/lamp/%d", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return Lamp{}
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return Lamp{}
	}

	var sensorData = Lamp{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return Lamp{}
	}

	fmt.Println("Response for GET Lamp:", string(body))
	return sensorData

}

var lamp Lamp = getLamp(8)

func SetLamp(id int) {
	lamp = getLamp(id)
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternBulb := "\\d+/bulb/set" // \\d+ matches one or more digits

	if string(msg.Payload()) == "ON" {
		if helper.IsTopicMatch(patternBulb, msg.Topic()) {
			fmt.Println("Topic is for Bulb set")
			changed := lamp.TurnBulbOn(client)
			// Command can be executed, is sent to backend
			if changed {
				lamp.bulbState = true
			}
		} else {
			changed := lamp.TurnOn(client, "ON")
			if changed {
				lamp.State = true
			}
		}
	}
	if string(msg.Payload()) == "OFF" {
		if helper.IsTopicMatch(patternBulb, msg.Topic()) {
			fmt.Println("Topic is for Bulb")
			changed := lamp.TurnBulbOff(client)
			if changed {
				lamp.bulbState = false
			}
		} else {
			changed := lamp.TurnOff(client, "OFF")
			if changed {
				lamp.State = false
			}
		}
	}

}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Printf("Connect lost: %v", err)
}

func RunLamp() {

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
	opts.SetUsername(constants.Username)
	opts.SetPassword(constants.Password)

	opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:  lamp.Id,
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
		panic(token.Error())
	}
	lamp.client = client

	lamp.Sub(client)
	lamp.SubToBulbSet(client)
	go pubLightSensorValue(client)
	lamp.SendHeartBeat(client)
}

func simulateLightSensor() int {

	// Daylight hours (06h to 18h)
	now := time.Now()
	hour := float64(now.Hour()) + float64(now.Minute())/60.0

	// Use sinus function for simulation of continous change
	intensity := int(70000*(math.Sin((hour-6.0)*math.Pi/12)) + rand.Float64()*5000)

	if intensity < 0 {
		intensity = int(rand.Int31n(1000))
	}

	lamp.lightSensorValue = intensity
	if lamp.lightSensorValue >= lightOnTreshold && lamp.bulbState {
		lamp.TurnBulbOff(lamp.client)
	}
	if lamp.lightSensorValue < lightOnTreshold && !lamp.bulbState {
		lamp.TurnBulbOn(lamp.client)
	}


	return intensity
}

func pubLightSensorValue(client mqtt.Client) {
	topic := fmt.Sprintf("%d/%s", lamp.Id, "light-sensor")
	fmt.Println("Topic for pub " + topic)
	for {
		ts := time.Now().UnixNano() / int64(time.Millisecond)
		data := lightSensorValue{
			Id:        strconv.Itoa(lamp.Id),
			Val:       strconv.Itoa(simulateLightSensor()),
			TimeStamp: strconv.FormatInt(ts, 10),
		}
		jsonData, err := json.Marshal(data)
		if err != nil {
			log.Fatal(err)
		}
		token := client.Publish(topic, 0, false, jsonData)
		token.Wait()

		if token.Error() != nil {
			log.Fatal(token.Error())
		}

		fmt.Println("Message from light sensor published successfully")

		time.Sleep(time.Second * constants.LightSensorReadPeriod)
	}
}

func (lamp Lamp) TurnBulbOn(client mqtt.Client) bool {
	myObj := bulbOn(lamp)
	topic := fmt.Sprintf("%d/%s", lamp.Id, "bulb")

	jsonData, err := json.Marshal(myObj)
	if err != nil {
		fmt.Println("JSON convert error - bulb")
	}
	fmt.Println(myObj)
	token := client.Publish(topic, 0, false, jsonData)
	token.Wait()
	return myObj.UsedFor != "Error"
}

func (lamp Lamp) TurnBulbOff(client mqtt.Client) bool {
	myObj := bulbOff(lamp)
	topic := fmt.Sprintf("%d/%s", lamp.Id, "bulb")

	jsonData, err := json.Marshal(myObj)
	if err != nil {
		fmt.Println("JSON convert error - bulb")
	}
	token := client.Publish(topic, 0, false, jsonData)
	token.Wait()
	return myObj.UsedFor != "Error"
}
