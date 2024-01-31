package charger

import (
	"encoding/json"
	"fmt"
	"io"
	"math/rand"
	"strconv"
	"strings"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"

	// "log"
	"net/http"
	device "tim10/mqtt/device"
	"tim10/mqtt/helper"
)

type Charger struct {
	device.BaseDevice
	ChargingStrength float64
	PortNum int32
	Percentage int32
	AvailablePortNumber int32

}


func (charger Charger) SubToPortSet(client mqtt.Client) {
	topic := fmt.Sprintf("charger/%d/%s", charger.Id, "port-set")
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {
	patternPort := "charger/\\d+/port-set"

	if helper.IsTopicMatch(patternPort, msg.Topic()) {
		fmt.Println(string(msg.Payload()))
		payload := string(msg.Payload())
		number, _ := strconv.Atoi(strings.Split(payload, ";")[0])
		if number > 0 && number <= 100 {
			pubChargerEvent(client, "percent change to " + strings.Split(payload, ";")[0], strings.Split(payload, ";")[1])
			charger.Percentage = int32(number)
		}
	}

}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
    fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
    fmt.Printf("Connect lost: %v", err)
}

func MakeCharger(id int32) Charger {
	apiURL := "http://localhost:8081/api/device/largeEnergy/charger/" + strconv.Itoa(int(id))

	// Make an HTTP GET request
	response, err := http.Get(apiURL)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return Charger{}
	}
	defer response.Body.Close()

	// Read the response body
	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return Charger{}
	}
	var PanelData =Charger{}
	err = json.Unmarshal(body, &PanelData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return Charger{}
	}
	// Print the response body as a string
	fmt.Println("Response:", string(body))
	return PanelData
}

var charger Charger = Charger{};

func RunCharger(id int32) {
	charger = MakeCharger(id)
	fmt.Println("Response:", charger.BaseDevice.Id)
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
		DeviceId:   charger.Id,
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

    charger.Sub(client)
	charger.SubToPortSet(client)
	go runChargerSimulation(client)
    charger.SendHeartBeat(client)
    client.Disconnect(500)
}

func runChargerSimulation(client mqtt.Client) {
	for {
		if charger.AvailablePortNumber > 0 {
			if (rand.Intn(101) > 50){
				charger.AvailablePortNumber -= 1
				go simulateCarComming(client);
			}
		}
		time.Sleep(time.Second * 5)
		
	}

}

func simulateCarComming(client mqtt.Client) {
	plateNumber := simulateLicencePlateRead()
	chargingLevel := rand.Intn(101)
	batteryCapacity := rand.Intn(156) + 45
	energy := 0.0
	pubChargerEvent(client, "charging-start", plateNumber)
	for i := 0; i <= int(charger.Percentage) - chargingLevel; i++ {
		sendElecticity(client)
		energy += charger.ChargingStrength/20
		pubChargingStatus(client, plateNumber, batteryCapacity, chargingLevel+i, energy)
		time.Sleep(time.Second * 3)
	}
	charger.AvailablePortNumber += 1
	pubChargerEvent(client, "charging-stop", plateNumber)

}

func sendElecticity(client mqtt.Client) {
	topic := fmt.Sprintf("energy/%d/%s", charger.Id, "any-device")
	data := fmt.Sprintf("energy-maintaining,device-id=%d,property-id=%d value=%f", charger.Id, charger.PropertyId, -charger.ChargingStrength)
	token := client.Publish(topic, 0, false, data)
	token.Wait()
}

func pubChargerEvent(client mqtt.Client, event string, caller string) {
	topic := fmt.Sprintf("charger/%d/%s", charger.Id, "event")
	// fmt.Println("Topic for pub " + topic)
	data := fmt.Sprintf("charger-event,device-id=%d value=\"%s\",caller=\"%s\"", charger.Id, event, caller)
	// charger-event,8 value=OPEN or CLOSE,caller=USER
	// charger-event,8 value=ENTER or LEAVE,caller=WR-131
	token := client.Publish(topic, 0, false, data)
	token.Wait()

	if token.Error() != nil {
		fmt.Println("charger event publish token error")
	}

}

func pubChargingStatus(client mqtt.Client, plateNumber string, batteryCapacity int, percentage int, energyConsumed float64) {
	topic := fmt.Sprintf("charger/%d/%s", charger.Id, "car")
	// fmt.Println("Topic for pub " + topic)
	data := fmt.Sprintf("charger-vehicle,device-id=%d,battery-capacity=%d,percentage=%d,energy-consumed=%f value=\"%s\"", charger.Id, batteryCapacity, percentage, energyConsumed, plateNumber)
	// charger-event,8 value=OPEN or CLOSE,caller=USER
	// charger-event,8 value=ENTER or LEAVE,caller=WR-131
	token := client.Publish(topic, 0, false, data)
	token.Wait()

	if token.Error() != nil {
		fmt.Println("charger event publish token error")
	}
}



func simulateLicencePlateRead() string {

	chars := "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	nums := "0123456789"

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