package lamp

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"math"
	"math/rand"
	"net/http"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

type Lamp struct {
	device.BaseDevice
	BulbState        bool `json:"bulbState"`
	IsAutomatic      bool `json:"automatic"`
	Latitude      	 float32 `json:"latitude"`
	Longitude      	 float32 `json:"longitude"`
	lightSensorValue int
	client           mqtt.Client
}

// Bulb is ON if light sensor registers less than 30000 lux
const lightOnTreshold = 30000

func bulbOn(lamp Lamp) device.MessageDTO {
	currentTime := time.Now()
	if lamp.BulbState {
		return device.MessageDTO{
			DeviceId:  lamp.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	lamp.BulbState = true
	return device.MessageDTO{
		DeviceId:  lamp.Id,
		UsedFor:   "ON",
		TimeStamp: currentTime,
	}
}

func bulbOff(lamp Lamp) device.MessageDTO {
	currentTime := time.Now()
	if !lamp.BulbState {
		return device.MessageDTO{
			DeviceId:  lamp.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	lamp.BulbState = false
	return device.MessageDTO{
		DeviceId:  lamp.Id,
		UsedFor:   "OFF",
		TimeStamp: currentTime,
	}
}

func automaticOn(lamp Lamp) device.MessageDTO {
	currentTime := time.Now()
	if lamp.IsAutomatic {
		return device.MessageDTO{
			DeviceId:  lamp.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	lamp.IsAutomatic = true
	return device.MessageDTO{
		DeviceId:  lamp.Id,
		UsedFor:   "ON",
		TimeStamp: currentTime,
	}
}

func automaticOff(lamp Lamp) device.MessageDTO {
	currentTime := time.Now()
	if !lamp.IsAutomatic {
		return device.MessageDTO{
			DeviceId:  lamp.Id,
			UsedFor:   "Error",
			TimeStamp: currentTime,
		}
	}
	lamp.IsAutomatic = false
	return device.MessageDTO{
		DeviceId:  lamp.Id,
		UsedFor:   "OFF",
		TimeStamp: currentTime,
	}
}

func (lamp Lamp) SubToBulbSet(client mqtt.Client) {
	topic := fmt.Sprintf("lamp/%d/%s", lamp.Id, "bulb/set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}

func (lamp Lamp) SubToAutomaticSet(client mqtt.Client) {
	topic := fmt.Sprintf("lamp/%d/%s", lamp.Id, "automatic/set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}


func GetLamp(deviceId int) Lamp {

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
	fmt.Println(sensorData)
	return sensorData

}

var lamp Lamp

func SetLamp(id int) {
	lamp = GetLamp(id)
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternOn := "\\d+"
	patternBulb := "lamp/\\d+/bulb/set"           // \\d+ matches one or more digits
	patternAutomatic := "lamp/\\d+/automatic/set" 

	if helper.IsTopicMatch(patternOn, msg.Topic()) {
		if string(msg.Payload()) == "ON" {
			changed := lamp.TurnOn(client, "ON")
			if changed {
				lamp.State = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := lamp.TurnOff(client, "OFF")
			if changed {
				lamp.State = false
			}
		}
	}

	if helper.IsTopicMatch(patternBulb, msg.Topic()) {
		fmt.Println("Topic is for Bulb set")
		if string(msg.Payload()) == "ON" {
			changed := lamp.TurnBulbOn(client)
			if changed {
				lamp.BulbState = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := lamp.TurnBulbOff(client)
			if changed {
				lamp.BulbState = false
			}
		}
	}

	if helper.IsTopicMatch(patternAutomatic, msg.Topic()) {
		fmt.Println("Topic is for Automatic set")
		if string(msg.Payload()) == "ON" {
			changed := lamp.TurnAutomaticOnOff(client, true)
			if changed {
				lamp.IsAutomatic = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := lamp.TurnAutomaticOnOff(client, false)
			if changed {
				lamp.IsAutomatic = false
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
	lamp.SubToAutomaticSet(client)
	go pubLightSensorValue(client)
	go lamp.TakesElectisity(client)
	go lamp.SendHeartBeat(client)
}

func simulateLightSensor() int {

	now := time.Now()
	hour := float64(now.Hour()) + float64(now.Minute())/60.0

	seasonHourCoeficient := 6.0
	seasonPiCoeficient := 12
	// Winter
	if now.Month() < 4 || now.Month() == 12 {
		seasonHourCoeficient = 6.0
		seasonPiCoeficient = 10
	// Spring/Autumn
	} else if (now.Month() >= 4 && now.Month() <= 6) || (now.Month() >= 9 && now.Month() < 12) {
		seasonHourCoeficient = 7.0
		seasonPiCoeficient = 12
	// Summer
	} else {
		seasonHourCoeficient = 5.0
		seasonPiCoeficient = 16
	}

	// Southern hemisphere, inverse seasons
	if (lamp.Latitude < 0) {
		// Summer
		if now.Month() < 4 || now.Month() == 12 {
			seasonHourCoeficient = 5.0
			seasonPiCoeficient = 16
		// Winter
		} else {
			seasonHourCoeficient = 6.0
			seasonPiCoeficient = 10
		}
	}

	fmt.Println("Season coefs: ", seasonHourCoeficient)
	
	// Use sinus function for simulation of continous change
	// Daylight hours (06h to 18h) will be taken into account, because sin()>0
	intensity := int(70000*(math.Sin((hour-seasonHourCoeficient)*math.Pi/float64(seasonPiCoeficient))) + rand.Float64()*5000)

	if intensity < 0 {
		intensity = int(rand.Int31n(1000))
	}

	lamp.lightSensorValue = intensity
	fmt.Println("Bulb state " , lamp.BulbState)
	fmt.Println("Automatic state " , lamp.IsAutomatic)
	if lamp.IsAutomatic && lamp.lightSensorValue >= lightOnTreshold && lamp.BulbState {
		changed := lamp.TurnBulbOff(lamp.client)
			if changed {
				lamp.BulbState = false
			}
	}
	if lamp.IsAutomatic && lamp.lightSensorValue < lightOnTreshold && !lamp.BulbState {
		changed := lamp.TurnBulbOn(lamp.client)
			if changed {
				lamp.BulbState = true
			}
	}

	return intensity
}

func pubLightSensorValue(client mqtt.Client) {
	topic := fmt.Sprintf("lamp/%d/%s", lamp.Id, "light-sensor")
	fmt.Println("Topic for pub " + topic)
	for {
		data := fmt.Sprintf("light-sensor,device-id=%d value=%d", lamp.Id, simulateLightSensor())
		token := client.Publish(topic, 0, false, data)
		token.Wait()

		if token.Error() != nil {
			log.Fatal(token.Error())
		}

		fmt.Println("Message from light sensor published successfully")

		time.Sleep(time.Second * constants.LightSensorReadPeriod)
	}
}

func pubBulbOnOff(client mqtt.Client, onOff bool) {
	topic := fmt.Sprintf("lamp/%d/%s", lamp.Id, "bulb-telemetry")
	fmt.Println("Topic for pub " + topic)
	val := 1;
	if (!onOff) {
		val = 0
	}
	data := fmt.Sprintf("bulb,device-id=%d value=%d", lamp.Id, val)
	token := client.Publish(topic, 0, false, data)
	token.Wait()

	if token.Error() != nil {
		log.Fatal(token.Error())
	}

	fmt.Println("Message from bulb published successfully")
}

func (lamp Lamp) TurnBulbOn(client mqtt.Client) bool {
	myObj := bulbOn(lamp)
	topic := fmt.Sprintf("lamp/%d/%s", lamp.Id, "bulb")

	jsonData, err := json.Marshal(myObj)
	if err != nil {
		fmt.Println("JSON convert error - bulb")
	}
	fmt.Println(myObj)
	token := client.Publish(topic, 0, false, jsonData)
	token.Wait()
	go pubBulbOnOff(client, true)
	return myObj.UsedFor != "Error"
}

func (lamp Lamp) TurnBulbOff(client mqtt.Client) bool {
	myObj := bulbOff(lamp)
	topic := fmt.Sprintf("lamp/%d/%s", lamp.Id, "bulb")

	jsonData, err := json.Marshal(myObj)
	if err != nil {
		fmt.Println("JSON convert error - bulb")
	}
	token := client.Publish(topic, 0, false, jsonData)
	token.Wait()
	go pubBulbOnOff(client, false)
	return myObj.UsedFor != "Error"
}

func (lamp Lamp) TurnAutomaticOnOff(client mqtt.Client, on bool) bool {
	var myObj device.MessageDTO
	if on {
		myObj = automaticOn(lamp)
	} else {
		myObj = automaticOff(lamp)
	}
	topic := fmt.Sprintf("lamp/%d/%s", lamp.Id, "automatic")

	jsonData, err := json.Marshal(myObj)
	if err != nil {
		fmt.Println("JSON convert error - automatic")
	}
	fmt.Println(myObj)
	token := client.Publish(topic, 0, false, jsonData)
	token.Wait()
	return myObj.UsedFor != "Error"
}
