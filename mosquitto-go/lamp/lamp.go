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
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"

)


const deviceName = "lamp"

type Lamp struct {
	device.BaseDevice
}

type lightSensorValue struct {
	Id 			string `json:"deviceId"`
	TimeStamp   string    `json:"timeStamp"`
	Val 		string `json:"value"`
}

func getLamp(deviceId int) Lamp {

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

var lamp Lamp = getLamp(8);

func SetLamp(id int) {
	lamp = getLamp(id);
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	
	if (string(msg.Payload()) == "ON"){
		changed := lamp.TurnOn(client, "ON")
		if (changed){
			lamp.State = true
		}
	}
	if (string(msg.Payload()) == "OFF"){
		changed := lamp.TurnOff(client, "OFF")
		if (changed){
			lamp.State = false
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
		UsedFor: "Kill",
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

	lamp.Sub(client)
	go pubLightSensorValue(client)
    lamp.SendHeartBeat(client)
}

func RunLampTelemetry() {

}

func pubLightSensorValue(client mqtt.Client) {
	topic := fmt.Sprint(lamp.Id)
	fmt.Println("topic for pub " + topic)
    num := 0
    for {
		if (num % 5 == 0) {
			val := num
			ts := time.Now().UnixNano()/int64(time.Millisecond)
			data := lightSensorValue {
				Id: strconv.Itoa(lamp.Id),
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


