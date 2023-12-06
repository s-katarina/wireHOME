package lamp

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"strconv"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"

)


const deviceName = "lamp"

type Lamp struct {
	device.BaseDevice
}

type lightSensorValue struct {
	TimeStamp   string    `json:"ts"`
	Val 		string `json:"val"`
}

func GetLamp(deviceId int) Lamp {

	apiUrl := fmt.Sprintf("%s/device/%d", constants.ApiUrl, deviceId)
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

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())

	patternOn := "simulation/lamp/\\d+/on" // \\d+ matches one or more digits
	patternOff := "simulation/lamp/\\d+/off" 

	if helper.IsTopicMatch(patternOn, msg.Topic()) {
		fmt.Println("Topic is for On command")
	} else if helper.IsTopicMatch(patternOff, msg.Topic()) {
		fmt.Println("Topic is for Off command")
	} else {
		fmt.Println("Topic does not match the pattern.")
	}

}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
    fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
    fmt.Printf("Connect lost: %v", err)
}

func InitConnections(lamp Lamp) {

	opts := mqtt.NewClientOptions()
    opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
    opts.SetUsername(constants.Username)
    opts.SetPassword(constants.Password)

    opts.SetDefaultPublishHandler(messagePubHandler)
    opts.OnConnect = connectHandler
    opts.OnConnectionLost = connectLostHandler

    client := mqtt.NewClient(opts)
    if token := client.Connect(); token.Wait() && token.Error() != nil {
        panic(token.Error())
    }

	subToOnCommand(client, lamp.Id)
	subToOffCommand(client, lamp.Id)
	pubLightSensorValue(client, lamp.Id)

}

func subToOnCommand(client mqtt.Client, id int) {
    topic := fmt.Sprintf("simulation/%s/%d/on", deviceName, id)
    token := client.Subscribe(topic, 1, nil)
    token.Wait()
  	fmt.Printf("Subscribed to topic: %s", topic)
}

func subToOffCommand(client mqtt.Client, id int) {
    topic := fmt.Sprintf("simulation/%s/%d/off", deviceName, id)
    token := client.Subscribe(topic, 1, nil)
    token.Wait()
  	fmt.Printf("Subscribed to topic: %s", topic)
}

func pubLightSensorValue(client mqtt.Client, id int) {
	topic := fmt.Sprintf("simulation/%s/%d/lightSensor", deviceName, id)
    num := 0
    for {
		if (num % 5 == 0) {
			val := num
			ts := time.Now().UnixNano()/int64(time.Millisecond)
			data := lightSensorValue {
				Val: strconv.Itoa(val),
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

			time.Sleep(time.Second)
		}
		num += 1
		time.Sleep(time.Second)
    }
}


