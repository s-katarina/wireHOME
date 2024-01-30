package washingMachine

import (
	"encoding/json"
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"io"
	"log"
	"net/http"
	"strconv"
	"strings"
	"tim10/mqtt/constants"
	"tim10/mqtt/device"
	"tim10/mqtt/helper"
	"time"
)

type WashingMachine struct {
	device.BaseDevice
	Regimes []string `json:"regimes"`
	CurrentAction string `json:"currentAction"`
	Client mqtt.Client
}

type WMTask struct {
	Id int32 `json:"id"`
	StartTime string `json:"startTime"`
	Action string `json:"action"`
}

var connectHandler mqtt.OnConnectHandler = func(client mqtt.Client) {
	fmt.Println("Connected")
}

var connectLostHandler mqtt.ConnectionLostHandler = func(client mqtt.Client, err error) {
	fmt.Printf("Connect lost: %v", err)
}

func getWashingMachine(deviceId int) WashingMachine {

	apiUrl := fmt.Sprintf("%s/washingMachine/%d", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return WashingMachine{}
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return WashingMachine{}
	}

	var sensorData = WashingMachine{}
	err = json.Unmarshal(body, &sensorData)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return WashingMachine{}
	}

	fmt.Println("Response for GET WashingMachine:", string(body))
	fmt.Println(sensorData)
	return sensorData

}

func getWMTasks(deviceId int32) []WMTask {
	apiUrl := fmt.Sprintf("%s/washingMachine/%d/wmtasks", constants.ApiUrl, deviceId)
	fmt.Println(apiUrl)

	response, err := http.Get(apiUrl)
	if err != nil {
		fmt.Println("Error making GET request:", err)
		return nil
	}
	defer response.Body.Close()

	body, err := io.ReadAll(response.Body)
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return nil
	}

	var wmtasks = []WMTask{}
	err = json.Unmarshal(body, &wmtasks)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return nil
	}

	fmt.Println("Response for GET WashingMachine WMTasks:", string(body))
	fmt.Println(wmtasks)
	return wmtasks
}

var washingMachine WashingMachine = getWashingMachine(6)
var wmtasks []WMTask = getWMTasks(6)
var onAutomatic = false
var email = ""

var messagePubHandler mqtt.MessageHandler = func(client mqtt.Client, msg mqtt.Message) {

	fmt.Printf("Received message: %s from topic: %s\n", msg.Payload(), msg.Topic())
	patternOn := "\\d+"

	if helper.IsTopicMatch(patternOn, msg.Topic()) {
		if string(msg.Payload()) == "ON" {
			changed := washingMachine.TurnOn(client, "ON")
			if changed {
				washingMachine.State = true
			}
		}
		if string(msg.Payload()) == "OFF" {
			changed := washingMachine.TurnOff(client, "OFF")
			if changed {
				washingMachine.State = false
			}
		}
	}

	if helper.IsTopicMatch("washingMachine/\\d+/request", msg.Topic()) {
		payload := string(msg.Payload())
		tokens := strings.Split(payload, ";")
		email = tokens[1]
		tokens = strings.Split(tokens[0], " ")
		action := strings.ToLower(tokens[1])

		supported := false
		for _, regime := range washingMachine.Regimes {
			if strings.Contains(regime, action) {
				supported = true
				break
			}
		}
		fmt.Println(action)

		if strings.Contains(action, "off") {
			supported = true
		}

		if strings.Contains(action, "automatic") {
			wmtasks = getWMTasks(int32(washingMachine.Id))
		}

		if supported {
			washingMachine.CurrentAction = action
			PubAction(action, email, client)
		} else {
			PubAction("Unsupported", "", client)
		}

		//onAutomatic = false
	}
}

func PubAction(action string, email string, client mqtt.Client) {
	topic := fmt.Sprintf("washingMachine/%d/response", washingMachine.Id)
	token := client.Publish(topic, 0, false, action + ";" + strconv.Itoa(washingMachine.Id))
	token.Wait()
	if token.Error() != nil {
		log.Fatal(token.Error())
	}
	fmt.Println("Message response published successfully")

	if action == "Unsupported" {
		return
	}

	PubEvent(action, email, client)
}

func PubEvent(action string, email string, client mqtt.Client) {
	topic := fmt.Sprintf("washingMachine/%d/event", washingMachine.Id)
	data := fmt.Sprintf("washingEvent,device-id=%d,email=%s value=\"%s\"", washingMachine.Id, email, action)
	token := client.Publish(topic, 0, false, data)
	token.Wait()
	if token.Error() != nil {
		log.Fatal(token.Error())
	}
	fmt.Printf("Message event published successfully\n")
}

func RunWashingMachine() {

	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:%d", constants.Broker, constants.Port))
	opts.SetUsername(constants.Username)
	opts.SetPassword(constants.Password)

	opts.SetDefaultPublishHandler(messagePubHandler)
	opts.OnConnect = connectHandler
	opts.OnConnectionLost = connectLostHandler

	myObj := device.MessageDTO{
		DeviceId:  washingMachine.Id,
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
	washingMachine.Client = client

	fmt.Println(washingMachine)
	washingMachine.Sub(client)
	washingMachine.SubToActionRequest(client)
	go washingMachine.TakesElectisity(client)
	go RunSim()
	washingMachine.SendHeartBeat(client)

}

func RunSim() {

	for {

		//if washingMachine.CurrentAction == "wool30" {
		//	//colling()
		//	fmt.Println("wool30")
		//}

		fmt.Println(washingMachine.CurrentAction)

		//if washingMachine.CurrentAction == "automatic" {
		//	if !onAutomatic {
		//		go runAutomatic()
		//	}
		//	onAutomatic = true
		//}


		time.Sleep(time.Second * 3)
	}

}

func (washingMachine WashingMachine) SubToActionRequest(client mqtt.Client) {
	topic := fmt.Sprintf("washingMachine/%d/request", washingMachine.Id)
	token := client.Subscribe(topic, 1, nil)
	token.Wait()
	fmt.Printf("Subscribed to topic: %s", topic)
}
