package ambientSensore

import (
	"encoding/json"
	"fmt"
	"io"
	"strconv"

	mqtt "github.com/eclipse/paho.mqtt.golang"

	// "log"
	"net/http"
	device "tim10/mqtt/device"
)

type AmbientSensore struct {
	device.BaseDevice
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {
	if (string(msg.Payload()) == "ON"){
		changed := ambientSensore.TurnOn(client, topicForBase)
		if (changed){
			ambientSensore.State = true
		}
	}
	if (string(msg.Payload()) == "OFF"){
		changed := ambientSensore.TurnOff(client, topicForBase)
		if (changed){
			ambientSensore.State = false
		}
	}
    fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
    fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
    fmt.Printf("Connect lost: %v", err)
}

func MakeAmbientSensore() AmbientSensore {
	apiURL := "http://localhost:8081/api/device/1"

	// Make an HTTP GET request
	response, err := http.Get(apiURL)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return AmbientSensore{}
	}
	defer response.Body.Close()

	// Read the response body
	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return AmbientSensore{}
	}

	var sensorData =AmbientSensore{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return AmbientSensore{}
	}
	// Print the response body as a string
	fmt.Println("Response:", string(body))
	return sensorData
}

var ambientSensore AmbientSensore = MakeAmbientSensore();
var topicForBase string = "simulation/ambientSensor";

func RunAmbientSensore() {
	
	fmt.Println("Response:", ambientSensore.BaseDevice.Id)
    var broker = "localhost"
    var port = 1883
    opts := mqtt.NewClientOptions()
    opts.AddBroker(fmt.Sprintf("tcp://%s:%d", broker, port))
    // opts.SetClientID("go_mqtt_client")
    opts.SetUsername("admin")
    opts.SetPassword("1234")
    opts.SetDefaultPublishHandler(messagePubHandler)
    opts.OnConnect = connectHandler
    opts.OnConnectionLost = connectLostHandler
    client := mqtt.NewClient(opts)
    if token := client.Connect(); token.Wait() && token.Error() != nil {
        panic(token.Error())
    }

    sub(client, ambientSensore.Id)
    ambientSensore.SendHeartBeat(client, topicForBase)

    client.Disconnect(500)
}

// func publish(client mqtt.Client) {
//     num := 10
//     for i := 0; i < num; i++ {
        
//         text := fmt.Sprintf("Message %d", i)
//         token := client.Publish("simulation/ambientSensor", 0, false, text)
//         token.Wait()
//         time.Sleep(time.Second)
//     }
// }

func sub(client mqtt.Client, id int) {
    topic := "ambientSensor" + strconv.Itoa(id)
    token := client.Subscribe(topic, 1, nil)
    token.Wait()
  fmt.Printf("Subscribed to topic: %s", topic)
}