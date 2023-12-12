package solarPanel

import (
	"encoding/json"
	"fmt"
	"io"
	"time"
	"github.com/sixdouglas/suncalc"
	"math"
	mqtt "github.com/eclipse/paho.mqtt.golang"

	// "log"
	"net/http"
	device "tim10/mqtt/device"
)

type SolarPanel struct {
	device.BaseDevice
	SurfaceSize float64
    Efficiency float64
    Latitude float64
	Longitude float64
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {
	if (string(msg.Payload()) == "ON"){
		changed := solarPanel.TurnOn(client, "ON")
		if (changed){
			solarPanel.State = true
		}
	}
	if (string(msg.Payload()) == "OFF"){
		changed := solarPanel.TurnOff(client, "OFF")
		if (changed){
			solarPanel.State = false
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

func MakeSolarPanel() SolarPanel {
	apiURL := "http://localhost:8081/api/device/solar/1"

	// Make an HTTP GET request
	response, err := http.Get(apiURL)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return SolarPanel{}
	}
	defer response.Body.Close()

	// Read the response body
	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return SolarPanel{}
	}

	var PanelData =SolarPanel{}
	err = json.Unmarshal(body, &PanelData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return SolarPanel{}
	}
	// Print the response body as a string
	fmt.Println("Response:", string(body))
	return PanelData
}

var solarPanel SolarPanel = MakeSolarPanel();

func RunSolarPanel() {
	
	fmt.Println("Response:", solarPanel.BaseDevice.Id)
    var broker = "localhost"
    var port = 1883
    opts := mqtt.NewClientOptions()
    opts.AddBroker(fmt.Sprintf("tcp://%s:%d", broker, port))
    opts.SetClientID("go_mqtt_client")
    opts.SetUsername("admin")
    opts.SetPassword("1234")
    opts.SetDefaultPublishHandler(messagePubHandler)
    opts.OnConnect = connectHandler
    opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:   solarPanel.Id,
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

    // solarPanel.Sub(client)
    // solarPanel.SendHeartBeat(client)
	sendElectisityByMinute(client)
    client.Disconnect(500)
}

func sendElectisityByMinute(client mqtt.Client) {
	// fmt.Printf("lag: %f step\n", solarPanel.Latitude)
	// fmt.Printf("lon: %f step\n", solarPanel.Longitude)
    var now = time.Now()

	var times = suncalc.GetTimes(now, solarPanel.Latitude, solarPanel.Longitude)

	if (now.Before(times[suncalc.Sunrise].Value) || now.After(times[suncalc.Sunset].Value)) {
		fmt.Println("nema sunca")
	}

	var sunPos  = suncalc.GetPosition(now, solarPanel.Latitude, solarPanel.Longitude);
	// fmt.Printf("Sun Azimuth: %f deg\n", sunPos.Azimuth*180/math.Pi)
	// fmt.Printf("Sun Altitude: %f deg\n", sunPos.Altitude*180/math.Pi)
	duration := times[suncalc.Sunset].Value.Sub(times[suncalc.Sunrise].Value)
	sunnyHours := duration.Hours()
	fmt.Println(sunnyHours)

	var efficiency = solarPanel.SurfaceSize * 1000 * solarPanel.Efficiency
	var outputByDay = efficiency * sunnyHours / 1000
	fmt.Printf("Output by day: %f\n",outputByDay)
	var outputByMinute = (outputByDay/(24*60)) * (1 - math.Abs(math.Cos(sunPos.Altitude*180/math.Pi)))
	fmt.Printf("Output by min: %f\n",outputByMinute)

	
	message := "Hello, MQTT!"
	token := client.Publish("energy;;", 0, false, message)
	token.Wait()

	fmt.Printf("Message sent: %s\n", message)

}	
