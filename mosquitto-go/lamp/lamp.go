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


type Lamp struct {
	device.BaseDevice
	bulbState bool
}

func bulbOn (lamp Lamp) device.MessageDTO {
    currentTime:= time.Now()
	if (lamp.bulbState) {
		return device.MessageDTO{
			DeviceId:   lamp.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		lamp.bulbState = true
		return device.MessageDTO{
			DeviceId:   lamp.Id,
			UsedFor: "ON",
			TimeStamp: currentTime,
		}
}

func bulbOff (lamp Lamp) device.MessageDTO{
    currentTime:= time.Now()
	if (!lamp.bulbState) {
		return device.MessageDTO{
			DeviceId:   lamp.Id,
			UsedFor: "Error",
			TimeStamp: currentTime,
		}
	}
		lamp.bulbState = false
		return device.MessageDTO{
			DeviceId:   lamp.Id,
			UsedFor: "OFF",
			TimeStamp: currentTime,
		}
}

func (lamp Lamp) SubToBulbSet (client mqtt.Client) {
	topic := fmt.Sprintf("%d/%s", lamp.Id, "bulb/set")
    token := client.Subscribe(topic, 1, nil)
    token.Wait()
  	fmt.Printf("Subscribed to topic: %s", topic)
}


type lightSensorValue struct {
	Id 			string `json:"deviceId"`
	TimeStamp   string    `json:"timeStamp"`
	Val 		string `json:"value"`
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

var lamp Lamp = getLamp(8);

func SetLamp(id int) {
	lamp = getLamp(id);
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternBulb := "\\d+/bulb/set" // \\d+ matches one or more digits
	
	if (string(msg.Payload()) == "ON"){
		if helper.IsTopicMatch(patternBulb, msg.Topic()) {
			fmt.Println("Topic is for Bulb set")
			changed := lamp.TurnBulbOn(client, fmt.Sprintf("%d/%s", lamp.Id, "bulb"))
			// Command can be executed, is sent to backend
			if (changed) { 
				lamp.bulbState = true
			}
		} else {
			changed := lamp.TurnOn(client, "ON")
			if (changed){
				lamp.State = true
			}
		}
	}
	if (string(msg.Payload()) == "OFF"){
		if helper.IsTopicMatch(patternBulb, msg.Topic()) {
			fmt.Println("Topic is for Bulb")
			changed := lamp.TurnBulbOff(client, fmt.Sprintf("%d/%s", lamp.Id, "bulb"))
			if (changed) {
				lamp.bulbState = false
			}
		} else {
			changed := lamp.TurnOff(client, "OFF")
			if (changed){
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
	lamp.SubToBulbSet(client)
	go pubLightSensorValue(client)
    lamp.SendHeartBeat(client)
}



func pubLightSensorValue(client mqtt.Client) {
	topic := fmt.Sprintf("%d/%s", lamp.Id, "light-sensor")
	fmt.Println("Topic for pub " + topic)
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
		time.Sleep(time.Second*3)
    }
}



func (lamp Lamp) TurnBulbOn(client mqtt.Client, topic string) bool{
	myObj := bulbOn(lamp)
	
	jsonData, err := json.Marshal(myObj)
	if err != nil {
		fmt.Println("JSON convert error - bulb")
	}
	fmt.Println(myObj)
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
	return myObj.UsedFor != "Error"
}

func (lamp Lamp) TurnBulbOff(client mqtt.Client, topic string) bool {
	myObj := bulbOff(lamp)
	
	jsonData, err := json.Marshal(myObj)
	if err != nil {
		fmt.Println("JSON convert error - bulb")
	}
    token := client.Publish(topic, 0, false, jsonData)
    token.Wait()
	return myObj.UsedFor != "Error"
}


