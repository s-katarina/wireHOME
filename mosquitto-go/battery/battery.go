package battery

import (
	"encoding/json"
	"fmt"
	"io"
	"strconv"
	"time"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	// "log"
	"net/http"
	device "tim10/mqtt/device"
)

type Battery struct {
	device.BaseDevice
	Capacity float64
	CurrentFill float64
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {
	
}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
    fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
    fmt.Printf("Connect lost: %v", err)
}

func MakeBattery(id int32) Battery {
	apiURL := "http://localhost:8081/api/device/largeEnergy/battery/" + strconv.Itoa(int(id))

	// Make an HTTP GET request
	response, err := http.Get(apiURL)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return Battery{}
	}
	defer response.Body.Close()

	// Read the response body
	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return Battery{}
	}
	var PanelData =Battery{}
	err = json.Unmarshal(body, &PanelData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return Battery{}
	}
	// Print the response body as a string
	fmt.Println("Response:", string(body))
	return PanelData
}

var battery Battery = Battery{};

func RunBattery(id int32) {
	battery = MakeBattery(id)
	fmt.Println("Response:", battery.BaseDevice.Id)
    var broker = "localhost"
    var port = 1883
    opts := mqtt.NewClientOptions()
    opts.AddBroker(fmt.Sprintf("tcp://%s:%d", broker, port))
    opts.SetUsername("admin")
    opts.SetPassword("1234")
    opts.SetDefaultPublishHandler(messagePubHandler)
    opts.OnConnect = connectHandler
    opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:   battery.Id,
		UsedFor: "Kill",
		TimeStamp: time.Now(),
	}

	// Convert the object to JSON
	jsonData, err := json.Marshal(myObj)
	if err != nil {
		// log.Fatal(err)
		fmt.Println("jbg")
	}
	opts.SetWill("KILLED", string(jsonData), 1, false)
    client := mqtt.NewClient(opts)
    if token := client.Connect(); token.Wait() && token.Error() != nil {
        panic(token.Error())
    }

    battery.Sub(client)
    battery.SendHeartBeat(client)
    client.Disconnect(500)
}

